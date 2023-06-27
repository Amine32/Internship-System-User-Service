package ru.tsu.hits.userservice.exception;

public class UserLacksFieldException extends RuntimeException{

    public UserLacksFieldException(String message) {
        super(message);
    }
}
