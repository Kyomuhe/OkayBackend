package com.ceres.project.services.auth;

import com.alibaba.fastjson2.JSONObject;
import com.ceres.project.config.ApplicationConf;
import com.ceres.project.config.JwtUtility;
import com.ceres.project.models.database.SystemUserModel;
import com.ceres.project.repositories.SystemUserRepository;
import com.ceres.project.services.base.BaseWebActionsService;
import com.ceres.project.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AuthService extends BaseWebActionsService {
    private final AuthenticationManager authenticationManager;
    private final ApplicationConf userDetailService;
    private final JwtUtility jwtUtility;
    private final SystemUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    OperationReturnObject returnObject = new OperationReturnObject();


    private OperationReturnObject login(JSONObject request){
        requires(request,"username","password");
        String username= request.getString("username");
        String password= request.getString("password");
        if(username.isEmpty() || username == null){
            return createErrorResponse(" username must not be empty");
        }
        if(password.isEmpty() || password == null){
            return createErrorResponse("password  must not be empty");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        final SystemUserModel userDetails = userDetailService.loadUserByUsername(username);
        final String token = jwtUtility.generateToken(userDetails);
        final String refreshToken = jwtUtility.generateRefreshToken(username);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token); // this is the jwt token the user can user from now on.
        response.put("user", userDetails);
        response.put("refreshToken", refreshToken);

//        OperationReturnObject res = new OperationReturnObject();
        returnObject.setReturnCodeAndReturnMessage(0, "Welcome back " + userDetails.getUsername());
        returnObject.setReturnObject(response);

        return returnObject;
    }

    private OperationReturnObject signup(JSONObject request) {
        try {
            requires(request,"username","password");
            String username = request.getString("username");
            String password = request.getString("password");
            String email = request.getString("email");
            String firstName = request.getString("firstName");
            String lastName = request.getString("lastName");

            if (username == null || username.isEmpty()) {
                return createErrorResponse("Username is either null or empty");
            }
            if (password == null || password.isEmpty()) {
                return createErrorResponse("Password is either null or empty");
            }
            if (email == null || email.isEmpty()) {
                return createErrorResponse("Email is either null or empty");
            }
            SystemUserModel existingUser = userRepository.findFirstByUsername(username);
            if(existingUser != null){
                return createErrorResponse("Username is already taken");
            }


            SystemUserModel user = new SystemUserModel();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setRoleCode("USER");
            user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            user.setIsActive(true);


            SystemUserModel savedUser = userRepository.save(user);

            final String accessToken = jwtUtility.generateToken(savedUser);
            final String refreshToken = jwtUtility.generateRefreshToken(username);

            Map<String, Object> response = new HashMap<>();
            response.put("token", accessToken);
            response.put("user", savedUser);
            response.put("refreshToken", refreshToken);

//            OperationReturnObject res = new OperationReturnObject();
            returnObject.setReturnCodeAndReturnMessage(0, "Account created successfully for " + savedUser.getUsername());
            returnObject.setReturnObject(response);

            return returnObject;

        } catch (RuntimeException e) {
            return createErrorResponse("Failed to create account: " + e.getMessage());
        }
    }

    public OperationReturnObject refreshToken(JSONObject request){
        try{
            String refreshToken = request.getString("refreshToken");

            if(!jwtUtility.isRefreshTokenValid(refreshToken)){
                return  createErrorResponse("Invalid refresh token");
            }
            String username = jwtUtility.extractUsername(refreshToken);

            SystemUserModel user = userRepository.findFirstByUsername(username);
            if(user == null || !user.getIsActive()){
                return  createErrorResponse("Invalid username or password");
            }
            String newAccessToken = jwtUtility.generateToken(user);
            String newRefreshToken = jwtUtility.generateRefreshToken(username);

            Map<String, Object> response = new HashMap<>();
            response.put("token", newAccessToken);
            response.put("user", user);
            response.put("refreshToken", newRefreshToken);

//            OperationReturnObject res = new OperationReturnObject();
            returnObject.setReturnCodeAndReturnMessage(0, "Account refreshed successfully for " + user.getUsername());
            returnObject.setReturnObject(response);
            return returnObject;

        }catch(RuntimeException e){
            return  createErrorResponse("Failed to refresh token: " + e.getMessage());
        }
    }

//    private OperationReturnObject createErrorResponse(String errorMessage) {
//        OperationReturnObject res = new OperationReturnObject();
//        res.setReturnCodeAndReturnMessage(1, errorMessage);
//        res.setReturnObject(null);
//        return res;
//    }


    @Override
    public OperationReturnObject switchActions(String action, JSONObject request) {
        return switch (action){
            case "login" -> login(request);
            case "signup" -> signup(request);
            case "refresh" -> refreshToken(request);
            default -> throw new IllegalArgumentException("Action " + action + " not known in this context");
        };
    }


}
