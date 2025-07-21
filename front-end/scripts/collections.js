//functie de populare cu colectii
async function getCollections() {
  const token = localStorage.getItem("token");
  const response = await fetch("http://localhost:1111/user-collection", {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  const data = await response.json();

  //golesc tot container ul cu colectii hard-codate
  const container = document.querySelector(".collections-container");
  container.innerHTML = "";

  const tipIcons = {
    1: "🪙", // monede
    2: "🖼️", // picturi
    3: "🎫", // timbre
    4: "🎶", // viniluri
    5: "📦", // custom
  };

  data.collections.forEach((col) => {
    const icon = tipIcons[col.idTip] || "📦"; // pune icon ul aferent
    const visibility = col.visibility ? "🌍 Public" : "🔒 Private"; // iau vizibilitatea
    const created = col.createdAt
      ? new Date(col.createdAt).toLocaleDateString()
      : "";

    const card = document.createElement("div");
    card.className = "collection-item";
    // card pentru afisarea unei colectii unde punem toate informatiile din request
    card.innerHTML = `
      <div class="collection-name-icon">  
        <span class="collection-name">${col.nume}</span>
        <span class="collection-icon">${icon}</span>
      </div>
      <div class="description-action-container">
      <p class="collection-description">
        ${visibility} &middot; Created: ${created}
      </p>
      <div class="collection-statistics">
        <p class="collection-price">👁️ ${col.total_views || 0}</p>
        <p class="collection-count">❤️ ${col.total_likes || 0}</p>
      </div>
      
      <div class="collection-actions">
        <a
          href="page_view-collection.html?id=${col.id}"
          class="button small-button collection-action"
        >
          View and Edit
        </a>
      </div>
      </div>
    `;
    container.appendChild(card);
  });

  // card pentru o colectie noua
  const createCard = document.createElement("div");
  createCard.className = "collection-item create-collection-item";
  createCard.innerHTML = `
    <div class="collection-name-icon">
      <span class="collection-name">Creează colecție</span>
      <span class="collection-icon">➕</span>
    </div>
    <p class="collection-description">
      Add a new collection to your profile
    </p>
    <div class="collection-actions">
      <a
        href="page_create-collection.html"
        class="button submit_button button small-button collection-action"
      >Create Collection</a>
    </div>
  `;
  container.appendChild(createCard);
}

//apelez functia de populare cu colectii
getCollections();
