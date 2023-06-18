package ru.tsu.hits.userservice.dto;

import lombok.Data;

@Data
public class UserDto {

    private String userId;

    private String firstName;

    private String lastName;

    private String patronym;

    private String role;

    private String email;
}
