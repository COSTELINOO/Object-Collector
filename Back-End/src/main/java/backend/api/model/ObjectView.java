package backend.api.model;

import java.sql.Date;
import java.sql.Time;

public class ObjectView {
    private Long id;
    private Long idObject;
    private Date data;
    private Time ora;
    private Long idUser;

    public ObjectView() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdObject() {
        return idObject;
    }

    public void setIdObject(Long idObject) {
        this.idObject = idObject;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public Time getOra() {
        return ora;
    }

    public void setOra(Time ora) {
        this.ora = ora;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }
}