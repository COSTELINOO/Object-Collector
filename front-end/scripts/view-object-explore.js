// script de populare a obiectului
document.addEventListener("DOMContentLoaded", async function () {
  // iau parametrii din url
  const params = new URLSearchParams(window.location.search);
  const objectId = params.get("id");
  const collectionId = params.get("collectionId");
  const token = localStorage.getItem("token");

  // iau datele de la back penru a popula pagina

  try {
    const res = await fetch(`http://localhost:1111/objects/${objectId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    const data = await res.json();
    const obj = data.object;
    if (!obj) return;

    // populare cu datele obiectului
    document.querySelector(".object-title").textContent = obj.numeObiect || "";
    document.querySelector(".object-description").textContent =
      obj.descriere || "";
    document.querySelector(".section-title").textContent = obj.numeObiect || "";
    document.getElementById(
      "main-image"
    ).src = `data:image/png;base64,${obj.image}`;

    document.getElementById("main-image").alt = obj.numeObiect || "";

    // vizibilitate
    const visBadge = document.querySelector(".visibility-badge");
    if (visBadge) {
      visBadge.textContent = obj.visibility ? "🌍 Public" : "🔒 Privat";

      //de modificat
      // visBadge.className =
      //   "visibility-badge " + (obj.visibility ? "public" : "private");
    }

    // populare valoare curenta
    document.querySelector(".value-amount.current").textContent =
      obj.pretAchizitie ? `$${obj.pretAchizitie}` : "$0";

    // populare cu statistici
    document.querySelector(
      ".stat-item.stat-item--views .stat-number"
    ).textContent = obj.views || "0";
    document.querySelector(
      ".stat-item.stat-item--likes .stat-number"
    ).textContent = obj.likes || "0";

    // popularea sectiunii de detalii
    const detailsGrid = document.querySelector(
      ".object-details-section .details-grid"
    );
    if (detailsGrid) {
      detailsGrid.innerHTML = ""; // goleste continutul existent

      const fieldLabels = {
        tara: "Country",
        an: "Year",
        material: "Material",
        greutate: "Weight",
        stare: "Condition",
        raritate: "Rarity",
        pret_achizitie: "Purchase Price",
        valoare: "Value",
        // de modificat
      };

      // construieste un array cu toate campurile vizibile si valorile lor
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

      // daca nu exista campuri vizile adaug mesajul corespunzator
      if (visibleFields.length === 0) {
        detailsGrid.innerHTML = `<div class="detail-item"><span class="detail-label">Fara detalii vizibile.</span></div>`;
      } else {
        // creez un singur grup cu toate valorile existente
        const groupDiv = document.createElement("div");
        groupDiv.className = "detail-group";
        groupDiv.innerHTML = `
                <h4 class="group-title">Object Details</h4>
                <div class="detail-items"></div>
              `;
        const detailItemsDiv = groupDiv.querySelector(".detail-items");

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
    console.error("Eroare la fetch pe obiect:", err);
  }
});
