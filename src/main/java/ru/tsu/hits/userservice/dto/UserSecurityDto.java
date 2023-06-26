package ru.tsu.hits.userservice.dto;

import lombok.Data;

@Data
public class UserSecurityDto {

    private String email;

    private String password;

    private String role;
}
