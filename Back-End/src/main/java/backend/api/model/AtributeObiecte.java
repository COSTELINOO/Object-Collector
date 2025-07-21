package backend.api.model;

import java.util.HashMap;
import java.util.Map;

public class AtributeObiecte {

    public static Map<String, Boolean>  getMonede() {
        Map<String, Boolean> visibleFields= new HashMap<>();
        visibleFields.put("material", true);
        visibleFields.put("valoare", true);
        visibleFields.put("greutate", true);
        visibleFields.put("nume_artist", false);
        visibleFields.put("tematica", false);
        visibleFields.put("gen", false);
        visibleFields.put("casa_discuri", false);
        visibleFields.put("tara", true);
        visibleFields.put("an", true);
        visibleFields.put("stare", true);
        visibleFields.put("raritate", true);
        visibleFields.put("pret_achizitie", true);
        return visibleFields;
    }

    public static Map<String, Boolean>  getTablouri() {
        Map<String, Boolean> visibleFields= new HashMap<>();
        visibleFields.put("material", false);
        visibleFields.put("valoare", false);
        visibleFields.put("greutate", false);
        visibleFields.put("nume_artist", true);
        visibleFields.put("tematica", false);
        visibleFields.put("gen", false);
        visibleFields.put("casa_discuri", false);
        visibleFields.put("tara", true);
        visibleFields.put("an", true);
        visibleFields.put("stare", true);
        visibleFields.put("raritate", true);
        visibleFields.put("pret_achizitie", true);
        return visibleFields;
    }

    public static Map<String, Boolean>  getTimbre() {
        Map<String, Boolean> visibleFields= new HashMap<>();
        visibleFields.put("material", false);
        visibleFields.put("valoare", false);
        visibleFields.put("greutate", false);
        visibleFields.put("nume_artist", false);
        visibleFields.put("tematica", false);
        visibleFields.put("gen", false);
        visibleFields.put("casa_discuri", false);
        visibleFields.put("tara", true);
        visibleFields.put("an", true);
        visibleFields.put("stare", true);
        visibleFields.put("raritate", true);
        visibleFields.put("pret_achizitie", true);
        return visibleFields;
    }

    public static Map<String, Boolean>  getViniluri() {
        Map<String, Boolean> visibleFields= new HashMap<>();
        visibleFields.put("material", false);
        visibleFields.put("valoare", false);
        visibleFields.put("greutate", false);
        visibleFields.put("nume_artist", true);
        visibleFields.put("tematica", false);
        visibleFields.put("gen", true);
        visibleFields.put("casa_discuri", true);
        visibleFields.put("tara", true);
        visibleFields.put("an", true);
        visibleFields.put("stare", true);
        visibleFields.put("raritate", true);
        visibleFields.put("pret_achizitie", true);
        return visibleFields;
    }

    public static Map<String, Boolean>  getCustom(Map<String, Boolean> obiect) {
        Map<String, Boolean> visibleFields= new HashMap<>();
        visibleFields.put("material", obiect.get("material"));
        visibleFields.put("valoare", obiect.get("valoare"));
        visibleFields.put("greutate", obiect.get("greutate"));
        visibleFields.put("nume_artist", obiect.get("nume_artist"));
        visibleFields.put("tematica", obiect.get("tematica"));
        visibleFields.put("gen", obiect.get("gen"));
        visibleFields.put("casa_discuri", obiect.get("casa_discuri"));
        visibleFields.put("tara", obiect.get("tara"));
        visibleFields.put("an", obiect.get("an"));
        visibleFields.put("stare", obiect.get("stare"));
        visibleFields.put("raritate", obiect.get("raritate"));
        visibleFields.put("pret_achizitie", obiect.get("pret_achizitie"));
        return visibleFields;
    }

    public static CustomCollection getCustomCollection(Map<String, Boolean> obiect) {
        CustomCollection customCollection = new CustomCollection();
        customCollection.setMaterial(obiect.get("material"));
        customCollection.setValoare(obiect.get("valoare"));
        customCollection.setGreutate(obiect.get("greutate"));
        customCollection.setNumeArtist(obiect.get("nume_artist"));
        customCollection.setTematica(obiect.get("tematica"));
        customCollection.setGen(obiect.get("gen"));
        customCollection.setCasaDiscuri(obiect.get("casa_discuri"));
        customCollection.setTara(obiect.get("tara"));
        customCollection.setAn(obiect.get("an"));
        customCollection.setStare(obiect.get("stare"));
        customCollection.setRaritate(obiect.get("raritate"));
        customCollection.setPretAchizitie(obiect.get("pret_achizitie"));
        return customCollection;
    }
}
