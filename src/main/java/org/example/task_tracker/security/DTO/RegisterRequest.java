package org.example.task_tracker.security.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
public class RegisterRequest {


    @NotBlank(message = "Username не может быть пустым")
    @Pattern(regexp = "^[a-zA-Z0-9_!@#$%&*()+=/:;|~.-]+$", message = "Username может содержать только латинские буквы, цифры и спецсимволы")
    private String username;

    @ToString.Exclude
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 8, max = 64, message = "Длина пароля должна составлять от 8 до 64 символов")
    private String password;

    @NotBlank(message = "Имя не может быть пустым")
    @Pattern(regexp = "^[ а-яА-ЯёЁa-zA-Z0-9_!@#$%&*()+=/:;|~.-]+$", message = "Имя может содержать кириллицу, латинские буквы, цифры и спецсимволы")
    private String name;
    
    @Email(message = "Неверно указан формат email")
    private String email;
}
