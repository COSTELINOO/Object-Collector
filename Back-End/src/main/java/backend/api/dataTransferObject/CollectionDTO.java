package backend.api.dataTransferObject;

import java.time.LocalDate;

public class CollectionDTO {
    Long id;
    String name;
    String username;
    String tipColectie;
    Long likes;
    Long view;
    Double value;
    Long count;
    LocalDate created;
    public CollectionDTO() {
        //nimic
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes;
    }

    public Long getView() {
        return view;
    }

    public void setView(Long view) {
        this.view = view;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getTipColectie() {
        return tipColectie;
    }

    public void setTipColectie(String tipColectie) {
        this.tipColectie = tipColectie;
    }

    public LocalDate getCreated() {
        return created;
    }

    public void setCreated(LocalDate created) {
        this.created = created;
    }
}
