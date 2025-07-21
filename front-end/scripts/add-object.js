document.addEventListener("DOMContentLoaded", async function () {
  const params = new URLSearchParams(window.location.search);
  const collectionId = params.get("collectionId");

  if (!collectionId) {
    console.warn("[DEBUG] Missing collectionId in URL. Aborting field setup.");
    return;
  }

  const token = localStorage.getItem("token");
  try {
    const res = await fetch(
      `http://localhost:1111/user-collection/${collectionId}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );

    const data = await res.json();
    const customFields =
      (data.collection && data.collection.customFields) || {};

    const fieldMap = {
      "object-material": "material",
      "object-value": "valoare",
      "object-weight": "greutate",
      "object-artist": "numeArtist",
      "object-publisher": "casaDiscuri",
      "object-theme": "tematica",
      "object-genre": "gen",
      "object-country": "tara",
      "object-year": "an",
      "object-condition": "stare",
      "object-rarity": "raritate",
      "object-purchase-price": "pretAchizitie",
    };

    Object.keys(fieldMap).forEach((inputId) => {
      const input = document.getElementById(inputId);
      if (input) {
        const fieldDiv = input.closest(".form-field");
        if (fieldDiv) fieldDiv.style.display = "none";
      }
    });

    Object.entries(fieldMap).forEach(([inputId, fieldKey]) => {
      if (customFields[fieldKey]) {
        const input = document.getElementById(inputId);
        if (input) {
          const fieldDiv = input.closest(".form-field");
          if (fieldDiv) fieldDiv.style.display = "";
        }
      }
    });

    ["object-name", "object-description", "object-image"].forEach((inputId) => {
      const input = document.getElementById(inputId);
      if (input) {
        const fieldDiv = input.closest(".form-field");
        if (fieldDiv) fieldDiv.style.display = "";
      }
    });
  } catch (err) {
    console.error("[DEBUG] Error fetching collection details:", err);
  }

  const addForm = document.getElementById("add-object-form");
  if (addForm) {
    addForm.addEventListener("submit", async function (e) {
      e.preventDefault();

      const payload = {
        name: document.getElementById("object-name").value,
        descriere: document.getElementById("object-description").value,
        material: document.getElementById("object-material").value,
        valoare: parseFloat(document.getElementById("object-value").value) || 0,
        greutate:
          parseFloat(document.getElementById("object-weight").value) || 0,
        numeArtist: document.getElementById("object-artist").value,
        tematica: document.getElementById("object-theme").value,
        gen: document.getElementById("object-genre").value,
        casaDiscuri: document.getElementById("object-publisher").value,
        tara: document.getElementById("object-country").value,
        an: document.getElementById("object-year").value,
        stare: document.getElementById("object-condition").value,
        raritate: document.getElementById("object-rarity").value,
        pretAchizitie:
          parseFloat(document.getElementById("object-purchase-price").value) ||
          0,
        visibility:
          document.getElementById("object-visibility").value === "true",
      };

      const imageInput = document.getElementById("object-image");
      const imageFile = imageInput && imageInput.files && imageInput.files[0];

      const formData = new FormData();
      // Object.entries(payload).forEach(([key, value]) => {
      //   formData.append(key, value);
      // });
      if (imageFile) {
        formData.append("image", imageFile);
      }
      formData.append("obiect", JSON.stringify(payload));

      console.log(formData);

      const params = new URLSearchParams(window.location.search);
      const collectionId = params.get("collectionId");

      if (!collectionId) {
        alert("Missing collectionId in URL!");
        return;
      }

      const token = localStorage.getItem("token");

      try {
        const res = await fetch(
          `http://localhost:1111/my-collection/${collectionId}/objects`,
          {
            method: "POST",
            headers: {
              Authorization: token ? `Bearer ${token}` : undefined,
            },
            body: formData,
          }
        );

        if (res.ok) {
          alert("Produsul a fost adăugat cu succes!");
          window.history.back();
        } else {
          alert("Eroare la adăugare! Verifică datele și încearcă din nou.");
        }
      } catch (err) {
        alert("Eroare de rețea sau server!");
      }
      console.log("Payload trimis:", payload);
      console.log("FormData object:", formData.get("obiect"));
      console.log("FormData image:", formData.get("image"));
    });
  }

  const imageInput = document.getElementById("object-image");
  const imageBtn = document.querySelector(".image-button-select");

  if (imageInput && imageBtn) {
    imageInput.addEventListener("change", function () {
      if (imageInput.files && imageInput.files[0]) {
        const fileName = imageInput.files[0].name;

        const dotIdx = fileName.lastIndexOf(".");
        let base = fileName;
        let ext = "";
        if (dotIdx !== -1) {
          base = fileName.substring(0, dotIdx);
          ext = fileName.substring(dotIdx);
        }

        const shortName = base.substring(0, 10) + ext;
        imageBtn.textContent = shortName;
      } else {
        imageBtn.textContent = "Choose Image";
      }
    });
  }
});

document.addEventListener("DOMContentLoaded", function () {
  const importBtn = document.getElementById("import-json-btn");
  const importInput = document.getElementById("import-json-input");

  if (importBtn && importInput) {
    importBtn.addEventListener("click", () => importInput.click());

    importInput.addEventListener("change", async function () {
      if (!importInput.files || !importInput.files[0]) return;
      const file = importInput.files[0];
      const reader = new FileReader();
      reader.onload = async function (e) {
        try {
          const json = JSON.parse(e.target.result);

          // Populează formularul cu datele din JSON
          // document.getElementById("object-name").value =
          // document.getElementById("object-description").value =

          // document.getElementById("object-material").value =

          // document.getElementById("object-value").value =
          // document.getElementById("object-weight").value =
          // document.getElementById("object-artist").value =

          // document.getElementById("object-theme").value =
          // document.getElementById("object-genre").value =
          // document.getElementById("object-publisher").value =

          // document.getElementById("object-country").value =
          // document.getElementById("object-year").value =
          // document.getElementById("object-condition").value =
          // document.getElementById("object-rarity").value =
          // document.getElementById("object-purchase-price").value =

          // document.getElementById("object-visibility").value =

          const payload = {
            // name: json.name || "",
            // descriere: json.descriere || "",
            // material: json.material || "",
            // valoare: json.valoare || "",
            // greutate: json.greutate || "",
            // numeArtist: json.numeArtist || "",
            // tematica: json.tematica || "",
            // gen: json.gen || "",
            // casaDiscuri: json.casaDiscuri || "",
            // tara: json.tara || "",
            // an: json.an || "",
            // stare: json.stare || "",
            // raritate: json.raritate || "",
            // pretAchizitie: json.pretAchizitie || "",
            // visibility: json.visibility ? "true" : "false",
          };

          if (json.name) payload.name = json.name;
          if (json.descriere) payload.descriere = json.descriere;
          if (json.material) payload.material = json.material;
          if (json.valoare) payload.valoare = json.valoare;
          if (json.greutate) payload.greutate = json.greutate;
          if (json.numeArtist) payload.numeArtist = json.numeArtist;
          if (json.tematica) payload.tematica = json.tematica;
          if (json.gen) payload.gen = json.gen;
          if (json.casaDiscuri) payload.casaDiscuri = json.casaDiscuri;
          if (json.tara) payload.tara = json.tara;
          if (json.an) payload.an = json.an;
          if (json.stare) payload.stare = json.stare;
          if (json.raritate) payload.raritate = json.raritate;
          if (json.pretAchizitie) payload.pretAchizitie = json.pretAchizitie;
          if (typeof json.visibility !== "undefined")
            payload.visibility = json.visibility ? "true" : "false";

          const formData = new FormData();
          formData.append("obiect", JSON.stringify(payload));
          formData.append("image", json.image || ""); // Dacă ai un câmp pentru imagine în JSON

          const params = new URLSearchParams(window.location.search);
          const collectionId = params.get("collectionId");
          if (!collectionId) {
            alert("Missing collectionId in URL!");
            return;
          }

          const token = localStorage.getItem("token");

          const res = await fetch(
            `http://localhost:1111/my-collection/${collectionId}/objects`,
            {
              method: "POST",
              headers: {
                Authorization: token ? `Bearer ${token}` : undefined,
              },
              body: formData,
            }
          );

          if (res.ok) {
            alert("Obiect importat cu succes!");
            window.location = `/page_view-collection.html?id=${collectionId}`;
          } else {
            alert("Eroare la import! Verifică datele și încearcă din nou.");
          }
        } catch (err) {
          alert("Fișier JSON invalid sau eroare la import!");
        }
      };
      reader.readAsText(file);
    });
  }
});
