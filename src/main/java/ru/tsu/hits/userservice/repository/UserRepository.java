package ru.tsu.hits.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tsu.hits.userservice.model.Role;
import ru.tsu.hits.userservice.model.UserEntity;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, String> {

    UserEntity findByEmail(String email);

    List<UserEntity> findByRole(Role role);
}
