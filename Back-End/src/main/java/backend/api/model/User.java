package backend.api.model;


import java.time.LocalDateTime;

import static backend.api.config.applicationConfig.Properties.getPath;

public class User {
    private Long id;
    private String username;
    private String password;
    private String email;
    private Long codeSession;
    private Long codeReset=0L;
    private String profilePicture=getPath()+"/default_profile_image/picture.png";
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getCodeSession() {
        return codeSession;
    }

    public void setCodeSession(Long codeSessio) {
        this.codeSession = codeSessio;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Long getCodeReset() {
        return codeReset;
    }

    public void setCodeReset(Long codeReset) {
        this.codeReset = codeReset;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}