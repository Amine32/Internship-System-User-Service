package ru.tsu.hits.userservice.dto.converter;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import ru.tsu.hits.userservice.dto.CreateUserDto;
import ru.tsu.hits.userservice.dto.UserDto;
import ru.tsu.hits.userservice.dto.WorkPlaceDto;
import ru.tsu.hits.userservice.model.UserEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@RequiredArgsConstructor
public class UserDtoConverter {

    private static final ModelMapper modelMapper = new ModelMapper();
    private static WebClient.Builder webClientBuilder;

    public static UserEntity convertDtoToEntity(CreateUserDto dto) {
        UserEntity userEntity = modelMapper.map(dto, UserEntity.class);

        userEntity.setId(UUID.randomUUID().toString());

        return userEntity;
    }

    public static UserDto convertEntityToDto(UserEntity entity) {
        return modelMapper.map(entity, UserDto.class);
    }

    public UserDto convertEntityToDetailedDto(UserEntity entity, HttpServletRequest request) {
        UserDto user = modelMapper.map(entity, UserDto.class);

        String authHeader = request.getHeader("Authorization");

        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + jwtToken);

            WorkPlaceDto workPlace = webClientBuilder.build()
                    .delete()
                    .uri("https://practice-service.onrender.com/api/workPlaceInfo/info/" + entity.getId())
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .retrieve()
                    .bodyToMono(WorkPlaceDto.class)
                    .block();

            assert workPlace != null;
            user.setCompanyName(workPlace.getCompanyName());
            user.setPosition(workPlace.getPosition());
        }

        return user;
    }
}
