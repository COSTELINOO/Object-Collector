package backend.api.model;

public enum CollectionType {

    MONEDE(1, "MONEDE"),
    TABLOURI(2, "TABLOURI"),
    TIMBRE(3, "TIMBRE"),
    VINILURI(4, "VINILURI"),
    CUSTOM(5, "CUSTOM");

    private final int id;
    private final String nume;

    CollectionType(int id, String nume) {
        this.id = id;
        this.nume = nume;
    }

    public int getId() {
        return id;
    }

    public String getNume() {
        return nume;
    }

    public static CollectionType getById(int id) {
        for (CollectionType type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Tipul de colectie cu ID-ul " + id + " nu exista");
    }
}