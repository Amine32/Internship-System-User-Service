package ru.tsu.hits.userservice.model;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Table(name = "groups")
public class GroupEntity {

    @Id
    private String groupNumber;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private List<UserEntity> students;
}
