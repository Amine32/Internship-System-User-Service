package ru.tsu.hits.userservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupDto {

    private String groupNumber;

    private List<UserDto> students;
}
