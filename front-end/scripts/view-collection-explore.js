//asteapta sa se incarca html ul
document.addEventListener("DOMContentLoaded", async function () {
  const token = localStorage.getItem("token");
  if (!token) return;
  // icon uri in functie de tipul colectiei
  const tipIcons = {
    1: "🪙", // monede
    2: "🖼️", // picturi
    3: "🎫", // timbre
    4: "🎶", // viniluri
    5: "📦", // custom
  };
  // etichete pentru toate tipurile de colectii
  const tipLabels = {
    1: "MONEDE",
    2: "PICTURI",
    3: "TIMBRE",
    4: "VINILURI",
    5: "CUSTOM",
  };

  const params = new URLSearchParams(window.location.search);

  const collectionId = params.get("id");
  if (!collectionId) {
    console.error("Id ul lipseste din url ");
    return;
  }

  //fetch pentru datele colectiei
  try {
    const res = await fetch(
      `http://localhost:1111/all-collection/${collectionId}`,
      {
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      }
    );
    const data = await res.json();

    console.log(data);

    // populare detalii colectie
    const col = data.collection;
    if (col) {
      document.querySelector(".collection-title").textContent = col.nume || "";
      document.querySelector(".collection-type").textContent =
        "Tip: " + (tipLabels[col.idTip] || "CUSTOM");
      document.querySelector(".collection-visibility").textContent =
        "Vizibilitate: " + (col.visibility ? "Publica" : "Privata");
      document.querySelector("#collection-icon").textContent =
        tipIcons[col.idTip] || "📦";
      document.querySelector(".collection-stats").innerHTML = `
        <span class="stat-item">${col.count} obiecte</span>
        <span class="stat-item">Valoare totală: $${col.value}</span>
        <span class="stat-item">${col.total_views} vizualizări</span>
        <span class="stat-item">${col.total_likes} aprecieri</span>
      `;
      document.querySelector(".section-title").textContent = col.nume || "";
      document.querySelector(".section-description").textContent =
        col.descriere || "";
    }

    // populeaza obiectele din colectie
    const grid = document.querySelector(".objects-grid");
    grid.innerHTML = ""; // le elimin pe cela hardcodate

    // pentru fiecare obiect din colectie creez un card unde adaug datele necesare
    data.objects.forEach((obj) => {
      const imgSrc = obj.image
        ? `data:image/png;base64,${obj.image}`
        : "/placeholder.svg?height=200&width=200";
      const card = document.createElement("div");
      card.className = "object-card";
      card.innerHTML = `
    <div class="object-image">
      <img src="${imgSrc}" alt="${obj.numeObiect}" />
    </div>
    <div class="object-info">
      <h3 class="object-name">${obj.numeObiect || ""}</h3>
      <p class="object-description">${obj.descriere || ""}</p>
      <div class="object-value">
        <span class="price">$${obj.pret_achizitie || "0"}</span>
      </div>
    </div>
    <div class="object-actions">
      <a href="page_view-object-explore.html?id=${
        obj.id
      }&collectionId=${collectionId}" class="action-btn view-btn">👁️</a>
    </div>
  `;
      grid.appendChild(card);
    });
  } catch (err) {
    console.error("eroare la fetch pe colectie:", err);
  }
});
