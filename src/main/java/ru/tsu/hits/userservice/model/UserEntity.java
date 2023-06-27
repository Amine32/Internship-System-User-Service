package ru.tsu.hits.userservice.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "users")
public class UserEntity {

    @Id
    private String id;

    private String email;

    private String password;

    private String firstName;

    private String lastName;

    private String patronym;

    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private GroupEntity group;

    private String companyId;
}
