package org.example.task_tracker.DTO.mapper;

import org.example.task_tracker.DTO.response.AdminUserResponseDTO;
import org.example.task_tracker.DTO.response.UserResponseDTO;
import org.example.task_tracker.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public UserResponseDTO toDTO(User user) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(user.getId());
        userResponseDTO.setName(user.getName());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setUsername(user.getUsername());
        return userResponseDTO;
    }

    public AdminUserResponseDTO toAdminDTO(User user) {
        AdminUserResponseDTO adminUserResponseDTO = new AdminUserResponseDTO();
        adminUserResponseDTO.setId(user.getId());
        adminUserResponseDTO.setName(user.getName());
        adminUserResponseDTO.setEmail(user.getEmail());
        adminUserResponseDTO.setUsername(user.getUsername());
        adminUserResponseDTO.setRole(user.getRole());
        return adminUserResponseDTO;
    }

    public List<UserResponseDTO> toDTOList(List<User> userList) {
        return userList.stream()
                .map(user -> toDTO(user))
                .toList();
    }

    public List<AdminUserResponseDTO> toAdminDTOList(List<User> userList) {
        return userList.stream()
                .map(user -> toAdminDTO(user))
                .toList();
    }
}
