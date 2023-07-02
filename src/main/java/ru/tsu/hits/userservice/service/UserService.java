package ru.tsu.hits.userservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import ru.tsu.hits.userservice.dto.CreateUserDto;
import ru.tsu.hits.userservice.dto.UpdateUserDto;
import ru.tsu.hits.userservice.dto.UserDto;
import ru.tsu.hits.userservice.dto.UserSecurityDto;
import ru.tsu.hits.userservice.dto.converter.UserDtoConverter;
import ru.tsu.hits.userservice.exception.TokenNotFoundException;
import ru.tsu.hits.userservice.exception.UserLacksFieldException;
import ru.tsu.hits.userservice.exception.UserNotFoundException;
import ru.tsu.hits.userservice.exception.WrongRoleException;
import ru.tsu.hits.userservice.model.Role;
import ru.tsu.hits.userservice.model.UserEntity;
import ru.tsu.hits.userservice.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WebClient.Builder webClientBuilder;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public UserDto signUp(CreateUserDto dto) {
        UserEntity userEntity = UserDtoConverter.convertDtoToEntity(dto);
        userEntity.setRole(Role.valueOf(dto.getRole()));

        //check if the user already exists
        if(userRepository.findByEmail(userEntity.getEmail()) != null) {
            throw new IllegalArgumentException("User already exists");
        }
        //encode the password and set it
        userEntity.setPassword(passwordEncoder.encode(dto.getPassword()));
        userEntity = userRepository.save(userEntity);

        if(userEntity.getRole() == Role.STUDENT) {
            if(userEntity.getGroup() != null) {
                webClientBuilder.build()
                        .post()
                        .uri("https://hits-application-service.onrender.com/api/students/" + userEntity.getId())
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
            }
            else {
                throw new UserLacksFieldException("Student requires a corresponding groupNumber");
            }
        }

        if(userEntity.getRole() == Role.COMPANY && userEntity.getCompanyId() == null) {
            throw new UserLacksFieldException("User belonging to company needs a company id");
        }

        return UserDtoConverter.convertEntityToDto(userEntity);
    }

    public void editUser(UserEntity user) {
        userRepository.save(user);
    }

    @Transactional
    public UserDto editUserById(String userId, UpdateUserDto dto) {
        UserEntity user = getUserById(userId);

        UserEntity checkEmail = getUserByEmail(dto.getEmail());
        if(!Objects.equals(user.getEmail(), dto.getEmail()) && checkEmail != null) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPatronym(dto.getPatronym());

        user = userRepository.save(user);

        return UserDtoConverter.convertEntityToDto(user);
    }

    @Transactional(readOnly = true)
    public UserEntity getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public UserDto getUserDtoById(String id) {
        return UserDtoConverter.convertEntityToDto(getUserById(id));
    }

    @Transactional(readOnly = true)
    public UserDto getUserDtoByEmail(String email) {
        return UserDtoConverter.convertEntityToDto(getUserByEmail(email));
    }

    @Transactional(readOnly = true)
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsersByRole(Role role) {
        List<UserEntity> users = userRepository.findAllByRole(role);
        List<UserDto> result = new ArrayList<>();

        users.forEach(element -> result.add(UserDtoConverter.convertEntityToDto(element)));

        return result;
    }

    @Transactional
    public void deleteUser(String id, HttpServletRequest request) {
        userRepository.deleteById(id);

        String authHeader = request.getHeader("Authorization");

        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + jwtToken);

            webClientBuilder.build()
                    .delete()
                    .uri("https://hits-application-service.onrender.com/api/students/" + id)
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        }
    }

    @Transactional
    public UserDto addCompany(String userId, String companyId) {
        UserEntity user = getUserById(userId);

        //Check if user has company role
        if(user.getRole() != Role.COMPANY) {
            throw new WrongRoleException("User does not have COMPANY role");
        }

        user.setCompanyId(companyId);
        user = userRepository.save(user);

        return UserDtoConverter.convertEntityToDto(user);
    }

    @Transactional
    public UserSecurityDto getUserSecurityDetails(String email) {
        UserEntity user = getUserByEmail(email);

        UserSecurityDto dto = new UserSecurityDto();
        dto.setEmail(user.getEmail());
        dto.setPassword(user.getPassword());
        dto.setRole(user.getRole().name());

        return dto;
    }

    @Transactional(readOnly = true)
    public UserDto getUserByToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);

            // Split the token into parts (header, payload, signature)
            String[] jwtParts = jwtToken.split("\\.");

            // Base64 decode the payload
            String payload = new String(Base64.getDecoder().decode(jwtParts[1]));

            // Convert payload JSON string to a Map
            Map<String, Object> payloadMap;
            try {
                payloadMap = new ObjectMapper().readValue(payload, new TypeReference<>() {
                });
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to decode payload", e);
            }

            // Get the 'sub' claim
            String email = (String) payloadMap.get("sub");

            return getUserDtoByEmail(email);
        }

        else {
            throw new TokenNotFoundException("Token not found");
        }
    }

}

