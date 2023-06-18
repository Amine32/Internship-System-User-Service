package ru.tsu.hits.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.tsu.hits.userservice.dto.CreateUpdateUserDto;
import ru.tsu.hits.userservice.dto.UserDto;
import ru.tsu.hits.userservice.model.Role;
import ru.tsu.hits.userservice.model.UserEntity;
import ru.tsu.hits.userservice.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-up")
    public UserDto signUp(@RequestBody CreateUpdateUserDto user) {
        return userService.signUp(user);
    }


    @GetMapping("/{id}")
    public UserEntity getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }

    @GetMapping("/{role}")
    public List<UserEntity> getUsersByRole(@PathVariable String role) {
        return userService.getUsersByRole(Role.valueOf(role));
    }
}
