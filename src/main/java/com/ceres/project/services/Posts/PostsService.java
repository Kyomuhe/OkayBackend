package com.ceres.project.services.Posts;

import com.alibaba.fastjson2.JSONObject;
import com.ceres.project.models.database.PostsModel;
import com.ceres.project.repositories.PostsRepository;
import com.ceres.project.services.base.BaseWebActionsService;
import com.ceres.project.utils.OperationReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostsService extends BaseWebActionsService {
    private final PostsRepository postsRepository;
    OperationReturnObject res = new OperationReturnObject();

    public OperationReturnObject createPost(JSONObject request){
        try{
            PostsModel post = new PostsModel();
            post.setTitle(request.getString("title"));
            post.setBody(request.getString("body"));
            post.setUserId(request.getLong("userId"));
            post.setCoverImage(request.getString("coverImage"));

//            byte[] imageBytes = request.getBytes("coverImage");
//            if(imageBytes != null && imageBytes.length>0){
//                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
//                        post.setCoverImage(base64Image);
//            }
            postsRepository.save(post);

            res.setCodeAndMessageAndReturnObject(0, "post created succussfully", post);
            return res;


        }catch(Exception e){
            e.printStackTrace();
            return createErrorResponse(e.getMessage());
        }
    }

    public OperationReturnObject displayAllPosts(){
        try{
            List<PostsModel> posts = postsRepository.findAll();
            res.setCodeAndMessageAndReturnObject(0, "All posts displayed succussfully", posts);
            return res;

        }catch(Exception e){
            return createErrorResponse(e.getMessage());
        }
    }

    public OperationReturnObject displayPost(JSONObject request){
        try{
            Long userId = request.getLong("userId");
            if(userId ==null){
                return createErrorResponse("A post must be submitted by a registered user");
            }
            List<PostsModel> posts =postsRepository.findByUserId(userId);
            if(posts ==null){
                return createErrorResponse("No posts yet");
            }
            res.setCodeAndMessageAndReturnObject(0, "retrieved posts succussfully",posts);
            return res;
        }catch(Exception e){
            return createErrorResponse(e.getMessage());
        }
    }
    public OperationReturnObject updatePost(JSONObject request) {
        try{
            Long postId = request.getLong("postId");
            PostsModel post = postsRepository.findById(postId).orElse(null);
            post.setTitle(request.getString("title"));
            post.setBody(request.getString("body"));
            post.setCoverImage(request.getString("coverImage"));

//            byte[] imageBytes = request.getBytes("coverImage");
//            if(imageBytes != null && imageBytes.length>0){
//                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
//                post.setCoverImage(base64Image);
//            }
            postsRepository.save(post);
            res.setCodeAndMessageAndReturnObject(0, "updated succussfully",post);
            return res;
        }catch(Exception e){
            return createErrorResponse(e.getMessage());
        }
    }

    public OperationReturnObject deletePost(JSONObject request){
        try{
            Long postId = request.getLong("postId");
            if(postId == null){
                return createErrorResponse("cannot delete a post which doesnot exist");
            }
            PostsModel post = postsRepository.findById(postId).orElse(null);
            postsRepository.delete(post);

            res.setReturnCodeAndReturnMessage(0,"deleted post succussfully");
            return res;

        }catch(Exception e){
            return createErrorResponse(e.getMessage());
        }
    }



    @Override
    public OperationReturnObject switchActions(String action, JSONObject request) {
        return switch (action){
            case "create" -> createPost(request);
            case "displayAllPosts" -> displayAllPosts();
            case "displayPost" -> displayPost(request);
            case "update" -> updatePost(request);
            case "delete" -> deletePost(request);
            default -> throw new IllegalArgumentException("Action " + action + " not known in this context");
        };
}
}
