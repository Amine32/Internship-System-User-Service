package ru.tsu.hits.userservice.exception;

public class WrongRoleException extends RuntimeException {

    public WrongRoleException(String message) {
        super(message);
    }
}
