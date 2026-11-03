package com.heladeria.api.dto;

public class LoginResponseDTO {

    private String token;
    private String refreshToken;
    private String username;
    private String rol;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, String username, String rol) {
        this(token, null, username, rol);
    }

    public LoginResponseDTO(String token, String refreshToken, String username, String rol) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.username = username;
        this.rol = rol;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
