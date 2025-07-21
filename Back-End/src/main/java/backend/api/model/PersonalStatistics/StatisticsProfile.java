package backend.api.model.PersonalStatistics;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsProfile {
   private Long id=0L;
    private String username="";
    private String email="";
    private LocalDate dataCrearii=null;
    private  LocalTime oraCrearii=null;
    private LocalDate dataActualizarii=null;
    private LocalTime oraActualizarii=null;
    private Long distinctLikes=0L;
    private Long distinctViews=0L;
    private Long totalLikes=0L;
    private Long totalViews=0L;
    private Double value=0.0;
    private Map<String,Long> mostLikedObject=new HashMap<>();
    private Map<String,Long> lessLikedObject=new HashMap<>();
    private Map<String,Long> mostViewedObject=new HashMap<>();
    private Map<String,Long> lessViewedObject=new HashMap<>();
    private Map<String,Long> mostLikedCollection=new HashMap<>();
    private Map<String,Long> lessLikedCollection=new HashMap<>();
    private Map<String,Long> mostViewedCollection=new HashMap<>();
    private Map<String,Long> lessViewedCollection=new HashMap<>();
    private Map<String,Double>mostValuableCollection=new HashMap<>();
    private Map<String,Double> lessValuableCollection=new HashMap<>();
    private Map<String,Double> mostValuableObject=new HashMap<>();
    private Map<String,Double> lessValuableObject=new HashMap<>();
    private List<StatisticsCollection> colectii=new ArrayList<>();

    public void addColection(StatisticsCollection statisticsCollection)
    {
        this.colectii.add(statisticsCollection);
    }

    public List<StatisticsCollection> getColectii() {
        return colectii;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public void setDistinctLikes() {
        long sum = 0L;
        for (StatisticsCollection statisticsCollection : colectii) {
            long likes = statisticsCollection.getDistinctLikes();
            sum += likes;
        }
        this.distinctLikes = sum;
    }

    public Long getDistinctViews() {
        return distinctViews;
    }

    public void setDistinctViews() {
        long sum = 0L;
        for (StatisticsCollection statisticsCollection : colectii) {
            long views = statisticsCollection.getDistinctViews();
            sum += views;
        }
        this.distinctViews = sum;
    }

    public Long getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes( ) {
        this.totalLikes = colectii.stream().mapToLong(StatisticsCollection::getTotalLikes).sum();
    }

    public Long getTotalViews() {
        return totalViews;
    }

    public void setTotalViews () {
        this.totalViews = colectii.stream().mapToLong(StatisticsCollection::getTotalViews).sum();
    }

    public Double getValue() {
        return value;
    }

    public void setValue( ) {
        this.value = colectii.stream().mapToDouble(StatisticsCollection::getTotalValuables).sum();
    }

    public Map<String,Long> getMostLikedObject() {

        return mostLikedObject;
    }

    public void setMostLikedObject() {
        String[] maxKey = {null};
        long[] maxLikes = {0};

        colectii.forEach(c -> {
            c.getMostLikedObject().forEach((key, value) -> {
                if (value > maxLikes[0]) {
                    maxLikes[0] = value;
                    maxKey[0] = key;
                }
            });
        });

        this.mostLikedObject = new HashMap<>();
        if (maxKey[0] != null) {
            this.mostLikedObject.put(maxKey[0], maxLikes[0]);
        }
    }

    public Map<String,Long> getLessLikedObject() {
        return lessLikedObject;
    }

    public void setLessLikedObject() {
        this.lessLikedObject = colectii.stream()
                .flatMap(c -> c.getMostLikedObject().entrySet().stream())
                .min(Map.Entry.comparingByValue())
                .map(e -> Map.of(e.getKey(), e.getValue()))
                .orElseGet(HashMap::new);
    }

    public Map<String,Long> getMostViewedObject() {
        return mostViewedObject;
    }

    public void setMostViewedObject() {
        this.mostViewedObject = colectii.stream()
                .flatMap(c -> c.getMostViewedObject().entrySet().stream())
                .max(Map.Entry.comparingByValue())
                .map(e -> Map.of(e.getKey(), e.getValue()))
                .orElseGet(HashMap::new);
    }

    public Map<String,Long> getLessViewedObject() {
        return lessViewedObject;
    }

    public void setLessViewedObject() {
        this.lessViewedObject = colectii.stream()
                .flatMap(c -> c.getMostViewedObject().entrySet().stream())
                .min(Map.Entry.comparingByValue())
                .map(e -> Map.of(e.getKey(), e.getValue()))
                .orElseGet(HashMap::new);
    }

    public Map<String,Double> getMostValuableObject() {
        return mostValuableObject;

    }

    public void setMostValuableObject() {
        this.mostValuableObject = colectii.stream()
                .flatMap(c -> c.getMostValuableObject().entrySet().stream())
                .max(Map.Entry.comparingByValue())
                .map(e -> Map.of(e.getKey(), e.getValue()))
                .orElseGet(HashMap::new);
    }

    public Map<String,Double> getLessValuableObject() {
        return lessValuableObject;
    }

    public void setLessValuableObject() {
        this.lessValuableObject = colectii.stream()
                .flatMap(c -> c.getMostValuableObject().entrySet().stream())
                .min(Map.Entry.comparingByValue())
                .map(e -> Map.of(e.getKey(), e.getValue()))
                .orElseGet(HashMap::new);
    }

    public Map<String,Double> getMostValuableCollection() {
        return mostValuableCollection;
    }

    public void setMostValuableCollection() {
        this.mostValuableCollection = colectii.stream()
                .collect(Collectors.toMap(
                        StatisticsCollection::getNume,
                        StatisticsCollection::getTotalValuables,
                        (v1, v2) -> v2                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> Map.of(entry.getKey(), entry.getValue()))
                .orElseGet(HashMap::new);
    }

    public Map<String,Double> getLessValuableCollection() {
        return lessValuableCollection;
    }

    public void setLessValuableCollection() {
        this.lessValuableCollection = colectii.stream()
                .collect(Collectors.toMap(
                        StatisticsCollection::getNume,
                        StatisticsCollection::getTotalValuables,
                        (v1, v2) -> v1
                ))
                .entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(entry -> Map.of(entry.getKey(), entry.getValue()))
                .orElseGet(HashMap::new);
    }

    public Map<String,Long> getMostLikedCollection() {
        return this.mostLikedCollection;
    }

    public void setMostLikedCollection() {
        this.mostLikedCollection = colectii.stream()
                .collect(Collectors.toMap(
                        StatisticsCollection::getNume,
                        StatisticsCollection::getTotalLikes,
                        (v1, v2) -> v2
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> Map.of(entry.getKey(), entry.getValue()))
                .orElseGet(HashMap::new);
    }

    public Map<String,Long> getLessLikedCollection() {
        return this.lessLikedCollection;
    }

    public void setLessLikedCollection() {
        this.lessLikedCollection = colectii.stream()
                .collect(Collectors.toMap(
                        StatisticsCollection::getNume,
                        StatisticsCollection::getTotalLikes,
                        (v1, v2) -> v1
                ))
                .entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(entry -> Map.of(entry.getKey(), entry.getValue()))
                .orElseGet(HashMap::new);
    }

    public Map<String,Long> getMostViewedCollection() {
        return this.mostViewedCollection;
    }

    public void setMostViewedCollection() {
        this.mostViewedCollection = colectii.stream()
                .collect(Collectors.toMap(
                        StatisticsCollection::getNume,
                        StatisticsCollection::getTotalViews,
                        (v1, v2) -> v1
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> Map.of(entry.getKey(), entry.getValue()))
                .orElseGet(HashMap::new);
    }

    public Map<String,Long> getLessViewedCollection() {
        return this.lessViewedCollection;
    }

    public void setLessViewedCollection() {
        this.lessViewedCollection = colectii.stream()
                .collect(Collectors.toMap(
                        StatisticsCollection::getNume,
                        StatisticsCollection::getTotalViews,
                        (v1, v2) -> v1
                ))
                .entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(entry -> Map.of(entry.getKey(), entry.getValue()))
                .orElseGet(HashMap::new);
    }

}
