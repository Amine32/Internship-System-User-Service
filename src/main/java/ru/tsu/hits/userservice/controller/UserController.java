package ru.tsu.hits.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.tsu.hits.userservice.dto.CreateUpdateUserDto;
import ru.tsu.hits.userservice.dto.UserDto;
import ru.tsu.hits.userservice.dto.UserSecurityDto;
import ru.tsu.hits.userservice.model.Role;
import ru.tsu.hits.userservice.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/users")
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

    @DeleteMapping("{id}")
    public void deleteUserById(@PathVariable String id, HttpServletRequest request) {
        userService.deleteUser(id, request);
    }

    @GetMapping("/security/{email}")
    public UserSecurityDto getUserSecurityDtoByEmail(@PathVariable String email) {
        return userService.getUserSecurityDetails(email);
    }

    @PostMapping("/company/{userId}/{companyId}")
    public UserDto setCompany(@PathVariable String userId, @PathVariable String companyId) {
        return userService.addCompany(userId, companyId);
    }
}
