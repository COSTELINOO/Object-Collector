package backend.api.model.GlobalStatistics;

public class Clasament {
    private Long id;
    private String name;
    private Double criteria;
    private Boolean visible;


    public String getInfo(Long index) {
        if(index==0)
            return this.id.toString();
        else  if(index==1)
            return this.name;
        else  if(index==2)
            return criteria.toString();
            return this.visible.toString();
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

    public Double getCriteria() {
        return criteria;
    }

    public void setCriteria(Double criteria) {
        this.criteria = criteria;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

@Override
    public String toString() {
        return ("id: "+id+"\nname: "+name+"\ncriteria: "+criteria+"\nvisible: "+visible);
}
}
