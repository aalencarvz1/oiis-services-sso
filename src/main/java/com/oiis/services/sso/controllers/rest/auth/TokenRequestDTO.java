package com.oiis.services.sso.controllers.rest.auth;

public class TokenRequestDTO {
    private String token; // Se token for nulo, a variável será criada como null

    // Getters e Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}