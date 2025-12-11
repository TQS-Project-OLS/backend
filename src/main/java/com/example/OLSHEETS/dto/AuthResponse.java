package com.example.OLSHEETS.dto;

public class AuthResponse {
    private String token;
    private String username;
    private String name;

    public AuthResponse() {
    }

    public AuthResponse(String token, String username, String name) {
        this.token = token;
        this.username = username;
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
