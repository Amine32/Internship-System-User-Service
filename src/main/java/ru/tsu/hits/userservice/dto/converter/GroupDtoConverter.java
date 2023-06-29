package ru.tsu.hits.userservice.dto.converter;

import org.modelmapper.ModelMapper;
import ru.tsu.hits.userservice.dto.GroupDto;
import ru.tsu.hits.userservice.dto.UserDto;
import ru.tsu.hits.userservice.model.GroupEntity;

import java.util.ArrayList;
import java.util.List;

public class GroupDtoConverter {

    private static final ModelMapper modelMapper = new ModelMapper();

    public static GroupDto convertEntityToDto(GroupEntity group) {
        GroupDto dto = modelMapper.map(group, GroupDto.class);

        List<UserDto> students = new ArrayList<>();

        if (group.getStudents() != null) {
            group.getStudents().forEach(element -> students.add(UserDtoConverter.convertEntityToDto(element)));
        }

        dto.setStudents(students);

        return dto;
    }
}
