// asteptam sa se incarce tot html ul  si fac add object
document.addEventListener("DOMContentLoaded", function () {
  const params = new URLSearchParams(window.location.search);
  const collectionId = params.get("id");
  const addObjectBtn = document.querySelector(".add-object-btn");
  if (addObjectBtn && collectionId) {
    addObjectBtn.href = `page_add-object.html?collectionId=${collectionId}`;
  }

  //adauga informatille despre colectie

  async function populateCollectionDetails() {
    //iau url si extrag id ul colectiei
    const params = new URLSearchParams(window.location.search);
    const collectionId = params.get("id");
    if (!collectionId) return;

    //iau token ul din localeStorage
    const token = localStorage.getItem("token");
    if (!token) return; // ies daca nu am token

    try {
      const response = await fetch(
        `http://localhost:1111/user-collection/${collectionId}`,
        {
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }
      );

      //verific raspunsul
      console.log(response);
      if (!response.ok) throw new Error("Eroare la fetch");
      const data = await response.json();
      const c = data.collection;

      // icon dupa tip
      const tipIcons = {
        1: "🪙", // monede
        2: "🖼️", // picturi
        3: "🎫", // timbre
        4: "🎶", // viniluri

        5: "📦", // custom
      };

      // iconita colectiei
      document.getElementById("collection-icon").textContent =
        tipIcons[c.idTip] || "📦";

      // titlul colectiei
      document.getElementById("collection-title").textContent = c.nume || "-";

      // tipul colectiei
      const tipLabels = {
        1: "MONEDE",
        2: "PICTURI",
        3: "TIMBRE",
        4: "VINILURI",
        5: "CUSTOM",
      };
      document.getElementById("collection-type").textContent =
        "Tip: " + (tipLabels[c.idTip] || "CUSTOM");

      // vizibilitate
      document.getElementById("collection-visibility").textContent =
        "Vizibilitate: " + (c.visibility ? "Publica" : "Privata");

      const objects = data.objects || [];
      const numObjects = objects.length;
      const totalValue = objects
        .map((obj) => Number(obj.valoare) || 0)
        .reduce((sum, val) => sum + val, 0);

      //nr de obiecte (trebuie modificat endpoint nou)

      document.getElementById("collection-objects").textContent =
        c.count + " obiecte";

      //valoare totala (trebuie modificat endpoint nou)

      document.getElementById("collection-value").textContent =
        "Valoare totala: $" + c.value;

      // vizualizari
      document.getElementById("collection-views").textContent =
        (c.total_views || 0) + " vizualizari";

      // aprecieri
      document.getElementById("collection-likes").textContent =
        (c.total_likes || 0) + " aprecieri";

      // link editare
      document.getElementById(
        "edit-collection-link"
      ).href = `page_edit-collection.html?id=${collectionId}`;

      const objectsGrid = document.querySelector(".objects-grid");
      if (objects.length === 0) {
        objectsGrid.innerHTML =
          "<p style='padding:2rem;'>Niciun obiect in aceasta colectie.</p>";
      } else {
        //  map imi returneaza un array de stringuri (fiecare string reprezinta un card de mai jos cum e)
        // creez cardurile pentru fiecare obiect si le pun in grid
        objectsGrid.innerHTML = objects
          .map(
            (obj) => `
          <div class="object-card">
            <div class="object-image">
              <img
            src="${
              obj.image
                ? `data:image/png;base64,${obj.image}`
                : "/placeholder.svg?height=200&width=200"
            }"
            alt="${obj.nume || "Obiect"}"
          >
            </div>
            <div class="object-info">
              <h3 class="object-name">${obj.descriere || "-"}</h3>
              <p class="object-description">
                ${obj.numeObiect || ""}
              </p>
              <div class="object-value">
                <span class="price">$${obj.pret_achizitie || 0}</span>
              </div>
            </div>
            <div class="object-actions">
              <a href="page_view-object.html?id=${
                obj.id
              }&collectionId=${collectionId}" class="action-btn view-btn">👁️</a>
              <a href="#" class="action-btn delete-btn" data-object-id="${
                obj.id
              }">🗑️</a>
            </div>
          </div>
        `
          )
          .join("");
      }
    } catch (err) {
      console.error("Eroare la incarcarea colectiei:", err);
    }
  }

  //populam colectia
  populateCollectionDetails();

  //stergere pentru obiect event listner
  document.addEventListener("click", async function (e) {
    //event pentru stergere obiect
    if (e.target.classList.contains("delete-btn")) {
      e.preventDefault();
      if (!confirm("Esti sigur ca vrei sa stregi obiectul?")) return;

      //iau id ul obuiectului si a colectiei
      const objectId = e.target.getAttribute("data-object-id");
      const params = new URLSearchParams(window.location.search);
      const collectionId = params.get("id");

      //validare de gasire la id
      if (!objectId || !collectionId) {
        alert("nu ai id pentru colectie sau object!");
        return;
      }

      // fac request la server pentru stergere
      try {
        const token = localStorage.getItem("token");
        const res = await fetch(
          `http://localhost:1111/my-collection/${collectionId}/objects/${objectId}`,
          {
            method: "DELETE",
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        //validare raspuns

        if (!res.ok) {
          const err = await res.text();
          alert("Eroare la stergearea obiectului: " + err);
          return;
        }
        // facem reload sa vedem schimbarile dupa stergere
        window.location.reload();
      } catch (err) {
        alert("eroare !");
      }
    }
  });
});
