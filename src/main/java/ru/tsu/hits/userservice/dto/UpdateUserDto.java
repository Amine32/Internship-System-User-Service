package ru.tsu.hits.userservice.dto;

import lombok.Data;

@Data
public class UpdateUserDto {

    private String firstName;

    private String lastName;

    private String patronym;

    private String email;
}
