package org.example.task_tracker.DTO.mapper;

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

    public List<UserResponseDTO> toDTOList(List<User> userList) {
        return userList.stream()
                .map(user -> toDTO(user))
                .toList();
    }
}
