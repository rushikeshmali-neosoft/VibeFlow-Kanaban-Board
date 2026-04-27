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
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.getIsActive());
        return dto;
    }
}