package backend.api.model.GlobalStatistics;
import java.util.*;
public class GeneralStatistics {
private  static Long totalCollections=0L;
private static Long totalObjects=0L;
private static Double totalValue=0.0;
private static Double lastMonth=0.0;
private static Double procentMonede=0.0;
private static Double procentTimbre=0.0;
private static Double procentTablouri=0.0;
private static Double procentVinil=0.0;
private static Double procentCustom=0.0;
private static List<Clasament>mostViewedCollections=new ArrayList<>();
private static List<Clasament>mostViewedObjects=new ArrayList<>();
private static List<Clasament>mostLikedCollections=new ArrayList<>();
private static List<Clasament>mostLikedObjects=new ArrayList<>();
private static List<Clasament>mostValuableCollections=new ArrayList<>();
private static List<Clasament>mostValuableObjects=new ArrayList<>();

public Long getTotalCollections() {
    return totalCollections;
}

public void setTotalCollections(Long totalCollections) {
    this.totalCollections = totalCollections;
}

public Long getTotalObjects() {
    return totalObjects;
}

public void setTotalObjects(Long totalObjects) {
    this.totalObjects = totalObjects;
}

public Double getTotalValue() {
    return totalValue;
}

public void setTotalValue(Double totalValue) {
    this.totalValue = totalValue;
}

public Double getLastMonth() {
    return lastMonth;
}

public void setLastMonth(Double lastMonth) {
    this.lastMonth = lastMonth;
}

public Double getProcentMonede() {
    return procentMonede;
}

public void setProcentMonede(Double procentMonede) {
    this.procentMonede = procentMonede;
}

public Double getProcentTimbre() {
    return procentTimbre;
}

public void setProcentTimbre(Double procentTimbre) {
    this.procentTimbre = procentTimbre;
}

public Double getProcentTablouri() {
    return procentTablouri;
}

public void setProcentTablouri(Double procentTablouri) {
    this.procentTablouri = procentTablouri;
}

public Double getProcentVinil() {
    return procentVinil;
}

public void setProcentVinil(Double procentVinil) {
    this.procentVinil = procentVinil;
}

public Double getProcentCustom() {
    return procentCustom;
}

public void setProcentCustom(Double procentCustom) {
    this.procentCustom = procentCustom;
}

public List<Clasament> getMostLikedObjects() {
        return mostLikedObjects;
    }
    public void setMostLikedObjects(List<Clasament> mostLikedObjects) {
    this.mostLikedObjects = mostLikedObjects;
    }

    public List<Clasament> getMostViewedCollections() {
    return mostViewedCollections;
}

public void setMostViewedCollections(List<Clasament> mostViewedCollections) {
    this.mostViewedCollections = mostViewedCollections;
}

public List<Clasament> getMostViewedObjects() {
    return mostViewedObjects;
}

public void setMostViewedObjects(List<Clasament> mostViewedObjects) {
    this.mostViewedObjects = mostViewedObjects;
}

public List<Clasament> getMostLikedCollections() {
    return mostLikedCollections;
}

public void setMostLikedCollections(List<Clasament> mostLikedCollections) {
    this.mostLikedCollections = mostLikedCollections;
}

public List<Clasament> getMostValuableCollections() {
    return mostValuableCollections;
}

public void setMostValuableCollections(List<Clasament> mostValuableCollections) {
    this.mostValuableCollections = mostValuableCollections;
}

public List<Clasament> getMostValuableObjects() {
    return mostValuableObjects;
}

public void setMostValuableObjects(List<Clasament> mostValuableObjects) {
    this.mostValuableObjects = mostValuableObjects;
}

}
