package backend.api.model;

import java.sql.Timestamp;
import java.util.Objects;

public class Collection {
    private Long id;
    private Long idUser;
    private Integer idTip;
    private String nume;
    private Boolean visibility;
    private Double value=0.0;
    private Long count =0L;
    private Timestamp createdAt;

    private CustomCollection customFields;

    public Collection() {
    }

    public Collection(Long idUser, Integer idTip, String nume, Boolean visibility) {
        this.idUser = idUser;
        this.idTip = idTip;
        this.nume = nume;
        this.visibility = visibility;
    }

    public boolean isMonedeType() {
        return idTip != null && idTip == 1;
    }

    public boolean isTablouriType() {
        return idTip != null && idTip == 2;
    }

    public boolean isTimbreType() {
        return idTip != null && idTip == 3;
    }

    public boolean isVinilType() {
        return idTip != null && idTip == 4;
    }

    public boolean isCustomType() {
        return idTip != null && idTip == 5;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public Integer getIdTip() {
        return idTip;
    }

    public void setIdTip(Integer idTip) {
        this.idTip = idTip;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public CustomCollection getCustomFields() {
        return customFields;
    }

    public void setCustomFields(CustomCollection customFields) {
        this.customFields = customFields;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Collection that = (Collection) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(idUser, that.idUser) &&
                Objects.equals(idTip, that.idTip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idUser, idTip);
    }
}