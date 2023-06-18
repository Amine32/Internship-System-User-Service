package ru.tsu.hits.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tsu.hits.userservice.model.GroupEntity;

public interface GroupRepository extends JpaRepository<GroupEntity, String> {
}
