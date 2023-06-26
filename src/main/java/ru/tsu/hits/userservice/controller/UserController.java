package ru.tsu.hits.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.tsu.hits.userservice.dto.CreateUpdateUserDto;
import ru.tsu.hits.userservice.dto.UserDto;
import ru.tsu.hits.userservice.model.Role;
import ru.tsu.hits.userservice.service.UserService;

import javax.servlet.http.HttpServletRequest;
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


    @GetMapping("/{email}")
    public UserDto getUserByEmail(@PathVariable String email) {
        return userService.getUserDtoByEmail(email);
    }

    @GetMapping("/roles/{role}")
    public List<UserDto> getUsersByRole(@PathVariable String role) {
        return userService.getUsersByRole(Role.valueOf(role));
    }

    @GetMapping("/jwt")
    public UserDto getUserByToken(HttpServletRequest request) {
        return userService.getUserByToken(request);
    }
}
