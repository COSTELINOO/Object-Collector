package backend.api.dataTransferObject;

import java.time.LocalDate;

public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String image;
    private String imageName;
    LocalDate created;
    Long countCollections;
    Long countObjects;
    Long views;
    Long likes;
    Double value;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public LocalDate getCreated() {
        return created;
    }

    public void setCreated(LocalDate created) {
        this.created = created;
    }

    public Long getCountCollections() {
        return countCollections;
    }

    public void setCountCollections(Long countCollections) {
        this.countCollections = countCollections;
    }

    public Long getCountObjects() {
        return countObjects;
    }

    public void setCountObjects(Long countObjects) {
        this.countObjects = countObjects;
    }

    public Long getViews() {
        return views;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
