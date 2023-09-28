package com.annika.entity;

import jakarta.inject.Singleton;

import java.util.stream.Collectors;

@Singleton
public class UserMapper {

    public UserDTO toDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(user.getUsername());
        if (user.getType() != null) {
            userDTO.setType(user.getType());
        }
        if (user.getAddress() != null) {
            userDTO.setAddress(user.getAddress());
        }
        return userDTO;
    }

    public User toEntity(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        if (userDTO.getType() != null) {
            user.setType(userDTO.getType());
        }
        if (userDTO.getAddress() != null) {
            user.setAddress(userDTO.getAddress());
        }
        return user;
    }
}
