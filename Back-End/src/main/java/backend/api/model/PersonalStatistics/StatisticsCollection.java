package backend.api.model.PersonalStatistics;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class StatisticsCollection {
    private String nume="";
    private String tipColectie="";
    private Boolean vizibilitate=Boolean.FALSE;
    private LocalDate dataCrearii=null;
    private LocalTime oraCrearii=null;
    private Map<String,Long> mostLikedObject=new HashMap<>();
    private Map<String,Long> mostViewedObject=new HashMap<>();
    private Map<String,Long> lessLikedObject=new HashMap<>();
    private Map<String,Long> lessViewedObject=new HashMap<>();
    private Long  distinctLikes=0L;
    private Long distinctViews=0L;
    private Long totalLikes=0L;
    private Long totalViews=0L;
    private Double totalValuables=0.0;
    private Map<String,Double> mostValuableObject=new HashMap<>();
    private Map<String,Double> lessValuableObject=new HashMap<>();
    private List<StatisticsObject> obiecte=new ArrayList<>();

    public void addObject(StatisticsObject statisticsObject)
    {
        this.obiecte.add(statisticsObject);
    }

    public List<StatisticsObject> getObiecte() {
        return obiecte;
    }

    public void setObiecte(List<StatisticsObject> obiecte) {
        this.obiecte = obiecte;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public String getTipColectie() {
        return tipColectie;
    }

    public void setTipColectie(String tipColectie) {
        this.tipColectie = tipColectie;
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

    public Map<String,Long> getMostLikedObject() {
        return mostLikedObject;
    }

    public void setMostLikedObject() {
        StatisticsObject statisticsObject = obiecte.stream()
                .max(Comparator.comparingLong(StatisticsObject::getTotalLikes))
                .orElse(null);
        if (statisticsObject != null) {
            this.mostLikedObject = Map.of(statisticsObject.getNume(), statisticsObject.getTotalLikes());
        } else {
            this.mostLikedObject = new HashMap<>();
        }
    }

    public Map<String,Long> getMostViewedObject() {
        return mostViewedObject;
    }

    public void setMostViewedObject() {
        StatisticsObject statisticsObject = obiecte.stream()
                .max(Comparator.comparingLong(StatisticsObject::getTotalViews))
                .orElse(null);
        if (statisticsObject != null) {
            this.mostViewedObject = Map.of(statisticsObject.getNume(), statisticsObject.getTotalViews());
        } else {
            this.mostViewedObject = new HashMap<>();
        }
    }

    public Long getDistinctLikes() {
        return distinctLikes;

    }

    public void setDistinctLikes() {
        this.distinctLikes = obiecte.stream()
                .mapToLong(StatisticsObject::getDistinctLikes)
                .sum();
    }

    public Long getDistinctViews() {
        return distinctViews;
    }

    public void setDistinctViews() {
        this.distinctViews = obiecte.stream()
                .mapToLong(StatisticsObject::getDistinctViews)
                .sum();
    }

    public Long getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes() {
        this.totalLikes = obiecte.stream()
                .mapToLong(StatisticsObject::getTotalLikes)
                .sum();
    }

    public Long getTotalViews() {
        return totalViews;
    }

    public void setTotalViews() {
        this.totalViews = obiecte.stream()
                .mapToLong(StatisticsObject::getTotalViews)
                .sum();
    }

    public Double getTotalValuables() {
        return totalValuables;
    }

    public void setTotalValuables() {
        totalValuables = obiecte.stream()
                .mapToDouble(StatisticsObject::getValue)
                .sum();
    }

    public Map<String,Double> getMostValuableObject() {
        return mostValuableObject;
    }

    public void setMostValuableObject() {
        StatisticsObject statisticsObject = obiecte.stream()
                .max(Comparator.comparingDouble(StatisticsObject::getValue))
                .orElse(null);
        if (statisticsObject != null) {
            this.mostValuableObject = Map.of(statisticsObject.getNume(), statisticsObject.getValue());
        } else {
            this.mostValuableObject = new HashMap<>();
        }
    }

    public Map<String,Double> getLessValuableObject() {
        return lessValuableObject;
    }

    public void setLessValuableObject() {
        StatisticsObject statisticsObject = obiecte.stream()
                .min(Comparator.comparingDouble(StatisticsObject::getValue))
                .orElse(null);
        if (statisticsObject != null) {
            this.lessValuableObject = Map.of(statisticsObject.getNume(), statisticsObject.getValue());
        } else {
            this.lessValuableObject = new HashMap<>();
        }
    }

    public void setLessLikedObject() {
        StatisticsObject statisticsObject = obiecte.stream()
                .min(Comparator.comparingLong(StatisticsObject::getTotalLikes))
                .orElse(null);
        if (statisticsObject != null) {
            this.lessLikedObject = Map.of(statisticsObject.getNume(), statisticsObject.getTotalLikes());
        } else {
            this.lessLikedObject = new HashMap<>();
        }
    }

    public Map<String,Long> getLessViewedObject() {
        return lessViewedObject;
    }

    public Map<String, Long> getLessLikedObject() {
        return lessLikedObject;
    }

    public void setLessViewedObject() {
        StatisticsObject statisticsObject = obiecte.stream()
                .min(Comparator.comparingLong(StatisticsObject::getTotalViews))
                .orElse(null);
        if (statisticsObject != null) {
            this.lessViewedObject = Map.of(statisticsObject.getNume(), statisticsObject.getTotalViews());
        } else {
            this.lessViewedObject = new HashMap<>();
        }
    }
}