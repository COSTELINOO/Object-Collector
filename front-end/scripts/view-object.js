var object = {};

// puleaza intreg obiectul cu datele vizibile

document.addEventListener("DOMContentLoaded", async function () {
  // ia parametrii din url
  const params = new URLSearchParams(window.location.search);
  const objectId = params.get("id");
  const collectionId = params.get("collectionId");
  const token = localStorage.getItem("token");

  // fech pentru date get

  try {
    const res = await fetch(
      `http://localhost:1111/my-collection/${collectionId}/objects/${objectId}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );
    const data = await res.json();
    console.log(data);
    const obj = data.myObject;
    object = obj; // salvez obiectul pentru export json
    if (!obj) return;

    // populez detaliile informative ale obiectului
    document.querySelector(".object-title").textContent = obj.numeObiect || "";
    document.querySelector(".object-description").textContent =
      obj.descriere || "";
    document.querySelector(".section-title").textContent = obj.numeObiect || "";
    document.getElementById("main-image").src = obj.image
      ? `data:image/png;base64,${obj.image}`
      : "/placeholder.svg?height=400&width=400";
    document.getElementById("main-image").alt = obj.numeObiect || "";

    // pun vizibilitatea obiectului
    const visBadge = document.querySelector(".visibility-badge");
    if (visBadge) {
      visBadge.textContent = obj.visibility ? "🌍 Public" : "🔒 Privat";
      visBadge.className =
        "visibility-badge " + (obj.visibility ? "public" : "private");
    }

    // valoarea curenta
    document.querySelector(".value-amount.current").textContent =
      obj.pretAchizitie ? `$${obj.pretAchizitie}` : "$0";

    // pupulare cu statisticile obiectului

    document.querySelector(
      ".stat-item.stat-item--views .stat-number"
    ).textContent = obj.views || "0";
    document.querySelector(
      ".stat-item.stat-item--likes .stat-number"
    ).textContent = obj.likes || "0";

    // adaug informatiile generale ale obiectului in sectiunea de detalii
    const detailsGrid = document.querySelector(
      ".object-details-section .details-grid"
    );
    if (detailsGrid) {
      detailsGrid.innerHTML = ""; // golesc card urile hardcodate

      const fieldLabels = {
        tara: "Country",
        an: "Year",
        material: "Material",
        greutate: "Weight",
        stare: "Condition",
        raritate: "Rarity",
        pret_achizitie: "Purchase Price",
        valoare: "Value",
        //label ur generale abiectului
      };

      //construiesc un array cu toate campurile vizibile ale obiectului si valoeare sa
      const visibleFields = Object.entries(obj.visibleFields || {})
        .filter(
          ([key, visible]) =>
            visible && obj[key] !== undefined && obj[key] !== null
        )
        .map(([key]) => {
          let value = obj[key];
          if (key === "pret_achizitie" || key === "valoare")
            value = `$${value}`;
          if (key === "greutate") value = `${value} g`;
          if (key === "an" && typeof value === "string" && value.length > 4)
            value = value.slice(0, 4);
          let label =
            fieldLabels[key] ||
            key.replace(/_/g, " ").replace(/^\w/, (c) => c.toUpperCase());
          return { key, label, value };
        });

      //daca nu am campuri adaug mesaj in grid
      if (visibleFields.length === 0) {
        detailsGrid.innerHTML = `<div class="detail-item"><span class="detail-label">No details available.</span></div>`;
      } else {
        // creez container uld de grid
        const groupDiv = document.createElement("div");
        groupDiv.className = "detail-group";
        groupDiv.innerHTML = `
                <h4 class="group-title">Object Details</h4>
                <div class="detail-items"></div>
              `;
        const detailItemsDiv = groupDiv.querySelector(".detail-items");

        // afisez fiecare proprietate in parte
        visibleFields.forEach(({ label, value }) => {
          const detailItem = document.createElement("div");
          detailItem.className = "detail-item";
          detailItem.innerHTML = `
                  <span class="detail-label">${label}:</span>
                  <span class="detail-value">${value}</span>
                `;
          detailItemsDiv.appendChild(detailItem);
        });

        detailsGrid.appendChild(groupDiv);
      }
    }
  } catch (err) {
    console.error("eroare la fetch pe obiect:", err);
  }
});

// export json al obiectului pentru a importa

document
  .getElementById("export-json-btn")
  .addEventListener("click", async function () {
    try {
      // creez un obiect json si initiez un blob pentru a exporta
      object.name = object.numeObiect;
      delete object.numeObiect; //elimin campul pentru a nu fi duplicate json
      const jsonStr = JSON.stringify(object, null, 2);
      const blob = new Blob([jsonStr], { type: "application/json" });
      const url = URL.createObjectURL(blob); // creez un url pentru blob

      const a = document.createElement("a");
      a.href = url;
      a.download = "object" + ".json";
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    } catch (err) {
      alert("fail la export");
      console.error("eroare la export json", err);
    }
  });
