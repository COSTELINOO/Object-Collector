package backend.api.model.PersonalStatistics;

import backend.api.model.Obiect;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class StatisticsObject
{
    private String nume="";
    private String descriere="Fara Descriere";
    private Boolean vizibilitate=Boolean.FALSE;
    private LocalDate dataCrearii=null;
    private LocalTime oraCrearii=null;
    private LocalDate dataActualizarii=null;
    private LocalTime oraActualizarii=null;
    private Long distinctLikes=0L;
    private Long distinctViews=0L;
    private Long totalLikes=0L;
    private Long totalViews=0L;
    private Double value=0.0;
    private Map<String, Boolean> visibleFields=new HashMap<>();
    private Obiect atribute=new Obiect();
    private String mostLiker="";
    private String mostViewer="";
    private String lastLiker="";
    private LocalDate  lastViewDate=null;
    private LocalTime lastViewTime=null;
    private String lastViewer="";
    private LocalDate lastLikedDate=null;
    private LocalTime lastLikedTime=null;

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public String getDescriere() {
        return descriere;
    }

    public void setDescriere(String descriere) {
        this.descriere = descriere;

    }

    public Boolean getVizibilitate() {
        return vizibilitate;
    }

    public void setVizibilitate(Boolean vizibilitate) {
        this.vizibilitate = vizibilitate;
    }

    public LocalDate getDataCrearii() {
        return dataCrearii;
    }

    public void setDataCrearii(LocalDate dataCrearii) {
        this.dataCrearii = dataCrearii;

    }

    public LocalTime getOraCrearii() {
        return oraCrearii;
    }

    public void setOraCrearii(LocalTime oraCrearii) {
        this.oraCrearii = oraCrearii;
    }

    public LocalDate getDataActualizarii() {
        return dataActualizarii;
    }

    public void setDataActualizarii(LocalDate dataActualizarii) {
        this.dataActualizarii = dataActualizarii;
    }

    public LocalTime getOraActualizarii() {
        return oraActualizarii;
    }

    public void setOraActualizarii(LocalTime oraActualizarii) {
        this.oraActualizarii = oraActualizarii;
    }

    public Long getDistinctLikes() {
        return distinctLikes;
    }

    public void setDistinctLikes(Long distinctLikes) {
        this.distinctLikes = distinctLikes;
    }

    public Long getDistinctViews() {
        return distinctViews;

    }

    public void setDistinctViews(Long distinctViews) {
        this.distinctViews = distinctViews;
    }

    public Long getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(Long totalLikes) {
        this.totalLikes = totalLikes;
    }

    public Long getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(Long totalViews) {
        this.totalViews = totalViews;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;

    }

    public Map<String, Boolean> getVisibleFields() {
        return visibleFields;
    }

    public void setVisibleFields(Map<String, Boolean> visibleFields) {
        this.visibleFields = visibleFields;
    }

    public Obiect getAtribute() {
        return atribute;
    }

    public void setAtribute(Obiect atribute) {
        this.atribute = atribute;
    }

    public String getMostLiker() {
        return mostLiker;
    }

    public void setMostLiker(String mostLiker) {
        this.mostLiker = mostLiker;
    }

    public String getMostViewer() {
        return mostViewer;
    }

    public void setMostViewer(String mostViewer) {
        this.mostViewer = mostViewer;
    }

    public String getLastLiker() {
        return lastLiker;
    }

    public void setLastLiker(String lastLiker) {
        this.lastLiker = lastLiker;
    }

    public LocalDate getLastViewDate() {
        return lastViewDate;
    }

    public void setLastViewDate(LocalDate lastViewDate) {
        this.lastViewDate = lastViewDate;
    }

    public LocalTime getLastViewTime() {
        return lastViewTime;
    }

    public void setLastViewTime(LocalTime lastViewTime) {
        this.lastViewTime = lastViewTime;
    }

    public String getLastViewer() {
        return lastViewer;
    }

    public void setLastViewer(String lastViewer) {
        this.lastViewer = lastViewer;
    }

    public LocalDate getLastLikedDate() {
        return lastLikedDate;
    }

    public void setLastLikedDate(LocalDate lastLikedDate) {
        this.lastLikedDate = lastLikedDate;
    }

    public LocalTime getLastLikedTime() {
        return lastLikedTime;
    }

    public void setLastLikedTime(LocalTime lastLikedTime) {
        this.lastLikedTime = lastLikedTime;
    }
}
