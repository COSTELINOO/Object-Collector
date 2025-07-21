package backend.api.model;

import java.time.LocalDate;
import java.sql.Timestamp;

public class Obiect {
    private Long id;
    private Integer idColectie;
    private String name ="NoName";
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
    private String raritate="None";
    private Double pretAchizitie=0.0;
    private String image;
    private Boolean visibility;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Obiect() {
        // nimic
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public float getValoare() {
        return valoare;
    }

    public void setValoare(Float valoare) {
        this.valoare = valoare;
    }

    public int getGreutate() {
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

    public Double getPretAchizitie() {
        return pretAchizitie;
    }

    public void setPretAchizitie(double pretAchizitie) {
        this.pretAchizitie = pretAchizitie;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Object{" +
                "id=" + id +
                ", idColectie=" + idColectie +
                ", numeColectie='" + name + '\'' +
                ", descriere='" + descriere + '\'' +
                ", material='" + material + '\'' +
                ", valoare=" + valoare +
                ", greutate=" + greutate +
                ", numeArtist='" + numeArtist + '\'' +
                ", tematica='" + tematica + '\'' +
                ", gen='" + gen + '\'' +
                ", casaDiscuri='" + casaDiscuri + '\'' +
                ", tara='" + tara + '\'' +
                ", an=" + an +
                ", stare='" + stare + '\'' +
                ", raritate='" + raritate + '\'' +
                ", pretAchizitie=" + pretAchizitie +
                ", image='" + image + '\'' +
                ", visibility=" + visibility +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}