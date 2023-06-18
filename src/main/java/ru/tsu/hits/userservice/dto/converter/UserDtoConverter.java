package ru.tsu.hits.userservice.dto.converter;

import org.modelmapper.ModelMapper;
import ru.tsu.hits.userservice.dto.CreateUpdateUserDto;
import ru.tsu.hits.userservice.dto.UserDto;
import ru.tsu.hits.userservice.model.UserEntity;

import java.util.UUID;

public class UserDtoConverter {

    private static final ModelMapper modelMapper = new ModelMapper();

    public static UserEntity convertDtoToEntity(CreateUpdateUserDto dto) {
        UserEntity userEntity = modelMapper.map(dto, UserEntity.class);

        userEntity.setId(UUID.randomUUID().toString());

        return userEntity;
    }

    public static UserDto convertEntityToDto(UserEntity entity) {
        return modelMapper.map(entity, UserDto.class);
    }
}
