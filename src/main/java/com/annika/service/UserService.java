package com.annika.service;

import com.annika.entity.*;
import com.annika.repository.UserProductRepository;
import com.annika.repository.UserRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.graalvm.nativebridge.In;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class UserService {
    @Inject
    private UserRepository userRepository;
    @Inject
    private UserMapper userMapper;
    @Inject
    private UserProductRepository userProductRepository;

    public UserDTO createUser(UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        userRepository.save(user);
        return userDTO;
    }

    public Optional <UserDTO> getUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(userMapper::toDTO);
    }

    public UserDTO getUserDetailAndProducts(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            UserDTO userDTO = userMapper.toDTO(user.get());
            List<UserProduct> userProducts = userProductRepository.findByUser(user.get());
            List<String> productNames = userProducts.stream()
                    .map(userProduct -> userProduct.getProduct().getProduct_name()
                    ).collect(Collectors.toList());
            userDTO.setProductNames(productNames);
            return userDTO;
        } else {
            return null;
        }
    }

    public List<UserDTO> getAllUsersAndProducts() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = new ArrayList<>();
        for (User user : users) {
            UserDTO userDTO = userMapper.toDTO(user);
            List<UserProduct> userProducts = userProductRepository.findByUser(user);
            List<String> productNames = userProducts.stream()
                    .map(userProduct -> userProduct.getProduct().getProduct_name()
                    ).collect(Collectors.toList());
            userDTO.setProductNames(productNames);
            userDTOs.add(userDTO);
        }
        return userDTOs;
    }

    public void updateUser(String username, UserDTO userDTO) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            User userToUpdate = user.get();
            userToUpdate.setType(userDTO.getType());
            userToUpdate.setAddress(userDTO.getAddress());
            userRepository.update(userToUpdate);
        }
    }

    public void deleteUser(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            userProductRepository.deleteByUser(user.get());
            userRepository.delete(user.get());
        }
    }
}
