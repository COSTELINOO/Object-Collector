package backend.api.model;

import java.sql.Timestamp;
import java.util.Objects;

public class CustomCollection {
    private Long id;
    private Long idColectie;
    private Boolean material;
    private Boolean valoare;
    private Boolean greutate;
    private Boolean numeArtist;
    private Boolean tematica;
    private Boolean gen;
    private Boolean casaDiscuri;
    private Boolean tara;
    private Boolean an;
    private Boolean stare;
    private Boolean raritate;
    private Boolean pretAchizitie;
    private Timestamp createdAt;

    public CustomCollection() {
        this.material = false;
        this.valoare = false;
        this.greutate = false;
        this.numeArtist = false;
        this.tematica = false;
        this.gen = false;
        this.casaDiscuri = false;
        this.tara = false;
        this.an = false;
        this.stare = false;
        this.raritate = false;
        this.pretAchizitie = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdColectie() {
        return idColectie;
    }

    public void setIdColectie(Long idColectie) {
        this.idColectie = idColectie;
    }

    public Boolean getMaterial() {
        return material;
    }

    public void setMaterial(Boolean material) {
        this.material = material;
    }

    public Boolean getValoare() {
        return valoare;
    }

    public void setValoare(Boolean valoare) {
        this.valoare = valoare;
    }

    public Boolean getGreutate() {
        return greutate;
    }

    public void setGreutate(Boolean greutate) {
        this.greutate = greutate;
    }

    public Boolean getNumeArtist() {
        return numeArtist;
    }

    public void setNumeArtist(Boolean numeArtist) {
        this.numeArtist = numeArtist;
    }

    public Boolean getTematica() {
        return tematica;
    }

    public void setTematica(Boolean tematica) {
        this.tematica = tematica;
    }

    public Boolean getGen() {
        return gen;
    }

    public void setGen(Boolean gen) {
        this.gen = gen;
    }

    public Boolean getCasaDiscuri() {
        return casaDiscuri;
    }

    public void setCasaDiscuri(Boolean casaDiscuri) {
        this.casaDiscuri = casaDiscuri;
    }

    public Boolean getTara() {
        return tara;
    }

    public void setTara(Boolean tara) {
        this.tara = tara;
    }

    public Boolean getAn() {
        return an;
    }

    public void setAn(Boolean an) {
        this.an = an;
    }

    public Boolean getStare() {
        return stare;
    }

    public void setStare(Boolean stare) {
        this.stare = stare;
    }

    public Boolean getRaritate() {
        return raritate;
    }

    public void setRaritate(Boolean raritate) {
        this.raritate = raritate;
    }

    public Boolean getPretAchizitie() {
        return pretAchizitie;
    }

    public void setPretAchizitie(Boolean pretAchizitie) {
        this.pretAchizitie = pretAchizitie;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomCollection that = (CustomCollection) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(idColectie, that.idColectie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idColectie);
    }
}