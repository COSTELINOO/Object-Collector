package backend.api.dataTransferObject;

import java.time.LocalDate;

public class ExplorerObjectDTO {
    Long id;
    String username;
    String tipColectie;
    String description;
    String image;
    String name;
    Long likes;
    Long views;
    Double value;
    LocalDate created;

    public  ExplorerObjectDTO() {
    //nimic
}

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

    public String getTipColectie() {
        return tipColectie;
    }

    public void setTipColectie(String tipColectie) {
        this.tipColectie = tipColectie;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public Long getViews() {
        return views;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public LocalDate getCreated() {
        return created;
    }

    public void setCreated(LocalDate created) {
        this.created = created;
    }
}
