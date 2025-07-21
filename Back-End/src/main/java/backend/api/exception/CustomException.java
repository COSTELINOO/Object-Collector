package backend.api.exception;

public class CustomException extends RuntimeException {
    private final String tip;
    private final String nume;
    private final String descriere;
    private final Throwable cauza;

    public CustomException(String tip, String nume, String descriere) {
        super(descriere);
        this.tip = tip;
        this.nume = nume;
        this.descriere = descriere;
        this.cauza = null;
    }

    public CustomException(String tip, String nume, Throwable cauza) {
        super(cauza);
        this.tip = tip;
        this.nume = nume;
        this.descriere = nume;
        this.cauza = cauza;
    }

    public String getNume() {
        return nume;
    }

    public String getDescriere() {
        return descriere;
    }

    public String getTip() {
        return tip;
    }

}