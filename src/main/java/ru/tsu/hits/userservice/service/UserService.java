package ru.tsu.hits.userservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tsu.hits.userservice.dto.CreateUpdateUserDto;
import ru.tsu.hits.userservice.dto.UserDto;
import ru.tsu.hits.userservice.dto.UserSecurityDto;
import ru.tsu.hits.userservice.dto.converter.UserDtoConverter;
import ru.tsu.hits.userservice.exception.TokenNotFoundException;
import ru.tsu.hits.userservice.exception.UserNotFoundException;
import ru.tsu.hits.userservice.model.Role;
import ru.tsu.hits.userservice.model.UserEntity;
import ru.tsu.hits.userservice.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public UserDto signUp(CreateUpdateUserDto dto) {
        UserEntity userEntity = UserDtoConverter.convertDtoToEntity(dto);
        userEntity.setRole(Role.valueOf(dto.getRole()));

        //check if the user already exists
        if(userRepository.findByEmail(userEntity.getEmail()) != null) {
            throw new IllegalArgumentException("User already exists");
        }
        //encode the password and set it
        userEntity.setPassword(passwordEncoder.encode(dto.getPassword()));
        userEntity = userRepository.save(userEntity);

        if(userEntity.getRole().toString().equals("STUDENT")) {
            Map<String, String> message = new HashMap<>();
            message.put("type", "StudentUserCreated");
            message.put("id", userEntity.getId());
            rabbitTemplate.convertAndSend("student.user.created", message);
        }

        return UserDtoConverter.convertEntityToDto(userEntity);
    }

    public void editUser(UserEntity user) {
        userRepository.save(user);
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
        List<UserEntity> users = userRepository.findByRole(role);
        List<UserDto> result = new ArrayList<>();

        users.forEach(element -> {
            result.add(UserDtoConverter.convertEntityToDto(element));
        });

        return result;
    }

    @Transactional
    public void deleteUser(String id) {
        userRepository.deleteById(id);

        Map<String, String> message = new HashMap<>();
        message.put("type", "UserDeleted");
        message.put("id", id);
        rabbitTemplate.convertAndSend("user.deleted", message);
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
            System.out.println(jwtToken);

            // Split the token into parts (header, payload, signature)
            String[] jwtParts = jwtToken.split("\\.");

            // Base64 decode the payload
            String payload = new String(Base64.getDecoder().decode(jwtParts[1]));

            // Convert payload JSON string to a Map
            Map<String, Object> payloadMap;
            try {
                payloadMap = new ObjectMapper().readValue(payload, new TypeReference<Map<String, Object>>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to decode payload", e);
            }

            // Get the sub claim
            String email = (String) payloadMap.get("sub");

            return getUserDtoByEmail(email);
        }

        else {
            throw new TokenNotFoundException("Token not found");
        }
    }

}

