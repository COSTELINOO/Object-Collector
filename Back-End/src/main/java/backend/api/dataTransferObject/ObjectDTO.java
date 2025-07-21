package backend.api.dataTransferObject;

import java.time.LocalDate;
import java.sql.Timestamp;
import java.util.Map;

public class ObjectDTO {
    private Long id;
    private Integer idColectie;
    private String numeObiect ="NoName";
    private String descriere="NoDescription";
    private String material="NoMaterial";
    private Float valoare=0.0f;
    private Integer greutate=0;
    private String numeArtist="NoName";
    private String tematica="None";
    private String gen="None";
    private String casaDiscuri="None";
    private String tara="NoCountry";
    private LocalDate an;
    private String stare="None";
    private String raritate="NoRarity";
    private Float pretAchizitie=0.0f;
    private Boolean visibility=false;
    private String image;
    private String imageName="NoName";
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Long views;
    private Long likes;
    private Map<String, Boolean> visibleFields;

    public Map<String, Boolean> getVisibleFields() {
        return visibleFields;
    }

    public void setVisibleFields(Map<String, Boolean> visibleFields) {
        this.visibleFields = visibleFields;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getIdColectie() {
        return idColectie;
    }

    public void setIdColectie(Integer idColectie) {
        this.idColectie = idColectie;
    }

    public String getNumeObiect() {
        return numeObiect;
    }

    public void setNumeObiect(String numeObiect) {
        this.numeObiect = numeObiect;
    }

    public String getDescriere() {
        return descriere;
    }

    public void setDescriere(String descriere) {
        this.descriere = descriere;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public Float getValoare() {
        return valoare;
    }

    public void setValoare(Float valoare) {
        this.valoare = valoare;
    }

    public Integer getGreutate() {
        return greutate;
    }

    public void setGreutate(Integer greutate) {
        this.greutate = greutate;
    }

    public String getNumeArtist() {
        return numeArtist;
    }

    public void setNumeArtist(String numeArtist) {
        this.numeArtist = numeArtist;
    }

    public String getTematica() {
        return tematica;
    }

    public void setTematica(String tematica) {
        this.tematica = tematica;
    }

    public String getGen() {
        return gen;
    }

    public void setGen(String gen) {
        this.gen = gen;
    }

    public String getCasaDiscuri() {
        return casaDiscuri;
    }

    public void setCasaDiscuri(String casaDiscuri) {
        this.casaDiscuri = casaDiscuri;
    }

    public String getTara() {
        return tara;
    }

    public void setTara(String tara) {
        this.tara = tara;
    }

    public LocalDate getAn() {
        return an;
    }

    public void setAn(LocalDate an) {
        this.an = an;
    }

    public String getStare() {
        return stare;
    }

    public void setStare(String stare) {
        this.stare = stare;
    }

    public String getRaritate() {
        return raritate;
    }

    public void setRaritate(String raritate) {
        this.raritate = raritate;
    }

    public Float getPretAchizitie() {
        return pretAchizitie;
    }

    public void setPretAchizitie(Float pretAchizitie) {
        this.pretAchizitie = pretAchizitie;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
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
}