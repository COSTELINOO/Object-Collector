// ascunde sau afiseaza sectiunea de atribute in functie de tipul colectiei pe care ne dorim sa fie creata
const typeSelect = document.getElementById("collection-type");
const attributesSection = document.getElementById("attributes-section");

function toggleAttributesSection() {
  if (typeSelect.value === "custom") {
    attributesSection.style.display = "";
  } else {
    attributesSection.style.display = "none";
  }
}

typeSelect.addEventListener("change", toggleAttributesSection);

// initializaeaza la incarcarea paginii
toggleAttributesSection();

//form submission
document
  .getElementById("create-collection-form")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    // iau valorile din formul de submit
    const collectionName = document.getElementById("collection-name").value;
    const collectionType = document.getElementById("collection-type").value;
    const collectionVisibility =
      document.getElementById("collection-visibility").value === "true";

    const selectedAttributes = Array.from(
      document.querySelectorAll('input[name="attributes"]:checked') // iar doar atributele selectate
    ).map((cb) => cb.value);

    //valisare nume colectie

    if (!collectionName) {
      alert("Introdu un nume pentru aceasta colectiei!");
      return;
    }

    const typeMap = {
      custom: 5,
      viniluri: 4,
      tablouri: 2,
      timbre: 3,
      monede: 1,
    };

    const idTip = typeMap[collectionType] || 5;

    let payload = {
      nume: collectionName,
      idTip: idTip,
      visibility: collectionVisibility,
    };

    if (collectionType === "custom") {
      // daca este custom, adauga atributele selectate
      const allFields = [
        "material",
        "valoare",
        "greutate",
        "nume_artist",
        "tematica",
        "gen",
        "casa_discuri",
        "tara",
        "an",
        "stare",
        "raritate",
        "pret_achizitie",
      ];

      // mapare pentru backend
      const fieldMap = {
        material: "material",
        valoare: "valoare",
        greutate: "greutate",
        nume_artist: "numeArtist",
        tematica: "tematica",
        gen: "gen",
        casa_discuri: "casaDiscuri",
        tara: "tara",
        an: "an",
        stare: "stare",
        raritate: "raritate",
        pret_achizitie: "pretAchizitie",
      };

      // pentru fiecare filed custom vad daca il gasesc in atributele selectate
      let customFields = {};
      allFields.forEach((field) => {
        customFields[fieldMap[field]] = selectedAttributes.includes(field);
      });
      payload.customFields = customFields;
    }

    // trimite post catre backend
    try {
      const token = localStorage.getItem("token");
      const response = await fetch("http://localhost:1111/user-collection", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      });
      if (!response.ok) {
        const err = await response.text();
        alert("Eroare la creare colectie: " + err);
        return;
      }
      alert(`Collection "${collectionName}" created successfully!`);
      window.location.href = "page_collections.html";
    } catch (err) {
      alert("Eroare la conectarea cu serverul!");
    }
  });
