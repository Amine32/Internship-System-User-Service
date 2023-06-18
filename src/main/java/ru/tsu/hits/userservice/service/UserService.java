package ru.tsu.hits.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tsu.hits.userservice.dto.CreateUpdateUserDto;
import ru.tsu.hits.userservice.dto.UserDto;
import ru.tsu.hits.userservice.dto.converter.UserDtoConverter;
import ru.tsu.hits.userservice.exception.UserNotFoundException;
import ru.tsu.hits.userservice.model.Role;
import ru.tsu.hits.userservice.model.UserEntity;
import ru.tsu.hits.userservice.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
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
    public List<UserEntity> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

}
