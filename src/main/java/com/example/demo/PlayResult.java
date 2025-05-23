package com.example.demo;

public class PlayResult {
    private final boolean success;
    private final String message;

    public PlayResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
