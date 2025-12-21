package com.ceres.project.config;

import com.ceres.project.models.database.SystemRoleModel;
import com.ceres.project.models.database.SystemUserModel;
import com.ceres.project.repositories.SystemRoleRepository;
import com.ceres.project.repositories.SystemUserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;

@Service
@Data
public class JwtUtility {

    private final SystemUserRepository userRepository;
    private final SystemRoleRepository roleRepository;

    @Value("${secret}")
    private String secret;

    public static final long JWT_TOKEN_VALIDITY = 12 * 60 * 60;
    public static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60; // 7 days

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractIssuedAt(String token){
        return extractClaim(token, Claims::getIssuedAt);
    }

    public String generateToken(UserDetails userDetails){
        return generateToken(new HashMap<>(), userDetails);
    }

//    refresh token
    public String generateRefreshToken(String username){
        long now =System.currentTimeMillis();

        return Jwts
                .builder()
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + REFRESH_TOKEN_VALIDITY * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    public boolean isRefreshTokenValid(String token){
        try{
            String username = extractUsername(token);
            return !extractExpiration(token).before(new Date()) && username != null;
        }catch(Exception e){
            return false;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token, username);
    }

    /**
     * @implNote Will check for two things if the token is expired as per eat claim
     * and will also check of the user has already requested for another token, requesting for another token
     * makes all the previously generated token expired.
     *
     * @param token jwt token
     * @param username username used when setting up userDetails, for our case, we use username
     * @return Boolean
     */
    private boolean isTokenExpired(String token, String username) {
        SystemUserModel usersModel = userRepository.findFirstByUsername(username);
        if (usersModel == null){
            throw new IllegalStateException("User not found");
        }else if(extractExpiration(token).before(new Date())){
            throw new IllegalStateException("SESSION EXPIRED");
        } else if (usersModel.getLastLoggedInAt() == null) {
            throw new IllegalStateException("INVALID TOKEN");
        } else if (extractIssuedAt(token).after(usersModel.getLastLoggedInAt())) {
            throw new IllegalStateException("EXPIRED TOKEN USED");
        } else if (Boolean.FALSE.equals(usersModel.getIsActive())){
            throw new IllegalStateException("ACCOUNT INACTIVE");
        }
        return false;
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String generateToken(Map<String, Object> claims, UserDetails userDetails){
        // truck the last time the token was generated -- the last time the user logged in
        long now = System.currentTimeMillis();
        Timestamp stamp = new Timestamp(now);

        SystemUserModel user = userRepository.findFirstByUsername(userDetails.getUsername());

        // archived staff members should not log in
        user.setLastLoggedInAt(stamp);
        userRepository.save(user);
        Optional<SystemRoleModel> rolesModel = roleRepository.findFirstByRoleCode(user.getRoleCode());
        if (rolesModel.isEmpty()){
            throw new IllegalStateException("Role not found for user: " + user.getUsername());
        }
        SystemRoleModel role = rolesModel.get();
        claims.put("role", role.getRoleName());
        claims.put("role_code", role.getRoleCode());
        claims.put("domain", role.getRoleDomain());
        List<String> permissions = new ArrayList<>();
        for (GrantedAuthority authority: userDetails.getAuthorities()){
            if (!permissions.contains(authority.getAuthority())){
                permissions.add(authority.getAuthority());
            }
        }
        claims.put("permissions", permissions);
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(user.getLastLoggedInAt())
                .setExpiration(new Date(now+ JWT_TOKEN_VALIDITY *1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
