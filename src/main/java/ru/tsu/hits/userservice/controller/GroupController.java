package ru.tsu.hits.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.tsu.hits.userservice.dto.CreateUpdateGroupDto;
import ru.tsu.hits.userservice.dto.GroupDto;
import ru.tsu.hits.userservice.service.GroupService;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public GroupDto createGroup(@RequestBody CreateUpdateGroupDto dto) {
        return groupService.createGroup(dto.getGroupNumber());
    }

    @GetMapping
    public List<GroupDto> getAllGroups() {
        return groupService.getAllGroups();
    }

    @GetMapping("/{groupNumber}")
    public GroupDto getGroup(@PathVariable String groupNumber) {
        return groupService.getGroup(groupNumber);
    }

    @DeleteMapping("/{groupNumber}")
    public void removeGroup(@PathVariable String groupNumber) {
        groupService.removeGroup(groupNumber);
    }


    @PostMapping("/{groupNumber}/{studentId}")
    public GroupDto addStudent(@PathVariable String groupNumber, @PathVariable String studentId) {
        return groupService.addStudent(studentId, groupNumber);
    }
}
