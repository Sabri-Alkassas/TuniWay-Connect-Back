package com.tuniway.connect.service;

import com.tuniway.connect.model.dto.RegisterClientRequest;
import com.tuniway.connect.model.dto.RegisterClientResponse;
import com.tuniway.connect.model.entity.AccountStatus;
import com.tuniway.connect.model.entity.Role;
import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.repository.UserRepository;

import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public RegisterClientResponse registerClient(RegisterClientRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (request.getPassword_hash() == null || request.getPassword_hash().isBlank()) {
            throw new RuntimeException("password_hash is required");
        }

        User user = new User();
        UUID uuid = UUID.randomUUID();
        user.setId(uuid);
        user.setEmail(request.getEmail());
        user.setPassword_hash(request.getPassword_hash());
        user.setRole(Role.CLIENT);
        user.setStatus(AccountStatus.ACTIVE);

        User savedUser = userRepository.save(user);
    
        RegisterClientResponse response = new RegisterClientResponse();
        response.setId(savedUser.getId());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole());
        response.setStatus(savedUser.getStatus());
        response.setCreatedAt(savedUser.getCreatedAt());
        response.setMessage("Client registered successfully");
        return response;
    }
}
