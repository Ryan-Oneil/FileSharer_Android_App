package biz.oneilindustries.filesharer.DTO;

import java.util.Optional;

public class User {

    private String username;
    private String password;
    private String refreshToken;
    private String authToken;

    public User(String username, String password, String refreshToken, String authToken) {
        this.username = username;
        this.password = password;
        this.refreshToken = refreshToken;
        this.authToken = authToken;
    }

    public User(String username, String password, String refreshToken) {
        this.username = username;
        this.password = password;
        this.refreshToken = refreshToken;
    }

    public User() {
    }

    public Optional<String> getUsername() {
        return Optional.ofNullable(username);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Optional<String> getRefreshToken() {
        return Optional.ofNullable(refreshToken);
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Optional<String> getAuthToken() {
        return Optional.ofNullable(authToken);
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", authToken='" + authToken + '\'' +
                '}';
    }
}
