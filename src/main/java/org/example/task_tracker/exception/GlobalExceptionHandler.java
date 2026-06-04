package org.example.task_tracker.exception;

import jakarta.validation.ConstraintViolationException;
import org.hibernate.type.descriptor.java.ObjectJavaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException e) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 404);
        map.put("message", e.getMessage());
        return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleInvalidData(HttpMessageNotReadableException e) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 400);
        map.put("message", "Неверный формат JSON или неверный тип данных одного из значений");
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleExceptions(Exception e) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 500);
        map.put("message", "Внутренняя ошибка сервера / Internal server error");
        return new ResponseEntity<Object>(map, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException e) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 400);
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        map.put("errors", errors);
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalState(IllegalStateException e) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 400);
        map.put("errors", e.getMessage());
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException e) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 400);
        List<String> errors = e.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .toList();
        map.put("errors", errors);
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException e) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 401);
        map.put("message", "Неверный username или password");
        return new ResponseEntity<>(map, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExists(UserAlreadyExistsException e) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 400);
        map.put("message", e.getMessage());
        return new ResponseEntity<>(map, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Object> handleUnauthorizedAccess(AuthorizationDeniedException e) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 403);
        map.put("message", e.getMessage());
        return new ResponseEntity<>(map, HttpStatus.FORBIDDEN);
    }

}
