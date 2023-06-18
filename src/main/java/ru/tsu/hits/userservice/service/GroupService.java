package ru.tsu.hits.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tsu.hits.userservice.dto.GroupDto;
import ru.tsu.hits.userservice.dto.converter.GroupDtoConverter;
import ru.tsu.hits.userservice.exception.GroupNotFoundException;
import ru.tsu.hits.userservice.model.GroupEntity;
import ru.tsu.hits.userservice.model.UserEntity;
import ru.tsu.hits.userservice.repository.GroupRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;

    private final UserService userService;

    @Transactional
    public GroupDto createGroup(String groupNumber) {
        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setGroupNumber(groupNumber);

        groupEntity = groupRepository.save(groupEntity);

        return GroupDtoConverter.convertEntityToDto(groupEntity);
    }

    @Transactional
    public GroupDto addStudent(String studentId, String groupNumber) {
        GroupEntity group = getGroupById(groupNumber);

        UserEntity student = userService.getUserById(studentId);
        student.setGroup(group);

        List<UserEntity> students = group.getStudents();
        students.add(student);

        group.setStudents(students);

        group = groupRepository.save(group);
        userService.editUser(student);

        return GroupDtoConverter.convertEntityToDto(group);
    }

    @Transactional(readOnly = true)
    public GroupDto getGroup(String groupNumber) {
        return GroupDtoConverter.convertEntityToDto(getGroupById(groupNumber));
    }

    @Transactional(readOnly = true)
    public List<GroupDto> getAllGroups() {
        List<GroupEntity> groups = groupRepository.findAll();

        List<GroupDto> result = new ArrayList<>();

        groups.forEach(element -> {
            result.add(GroupDtoConverter.convertEntityToDto(element));
        });

        return result;
    }

    @Transactional(readOnly = true)
    public void removeGroup(String groupNumber) {
        groupRepository.delete(getGroupById(groupNumber));
    }

    private GroupEntity getGroupById(String groupNumber) {
        return groupRepository.findById(groupNumber)
                .orElseThrow(() -> new GroupNotFoundException("Group with number " + groupNumber + " not found"));
    }
}
