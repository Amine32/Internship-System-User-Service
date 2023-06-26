package ru.tsu.hits.userservice.exception;

public class StudentAlreadyBelongsToGroup extends RuntimeException {

    public StudentAlreadyBelongsToGroup(String message) {
        super(message);
    }
}
