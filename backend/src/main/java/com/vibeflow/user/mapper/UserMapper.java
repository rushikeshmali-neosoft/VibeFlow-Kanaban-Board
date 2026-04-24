package com.vibeflow.user.mapper;

import com.vibeflow.auth.entity.User;
import com.vibeflow.user.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(user.getId(), user.getEmail());
    }
}