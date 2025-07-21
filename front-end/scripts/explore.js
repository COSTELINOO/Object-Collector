var counter = 0;

function updateQueryParamsFromFilters() {
  const params = new URLSearchParams(window.location.search);

  params.set("contentType", currentFilters.contentType || "collections");
  params.set("category", currentFilters.category || "");
  params.set("sort", currentFilters.sort || "");
  params.set("createdAfter", currentFilters.createdAfter || "");

  ["category", "sort", "createdAfter"].forEach((key) => {
    if (!currentFilters[key]) params.delete(key);
  });

  const searchValue = document.querySelector(".search-field").value.trim();
  if (searchValue) {
    params.set("search", searchValue);
  } else {
    params.delete("search");
  }

  const newUrl =
    window.location.pathname +
    (params.toString() ? "?" + params.toString() : "");
  window.history.replaceState({}, "", newUrl);
}

function updateFiltersFromQueryParams() {
  const params = new URLSearchParams(window.location.search);
  currentFilters = {
    contentType: params.get("contentType") || "collections",
    category: params.get("category") || "",
    sort: params.get("sort") || "",
    createdAfter: params.get("createdAfter") || "",
  };

  console.log(
    document.querySelector(
      `input[name="category"][value="${currentFilters.category}"]`
    )
  );
  document.querySelector(
    `input[name="content-type"][value="${currentFilters.contentType}"]`
  ).checked = true;

  document.querySelector(
    `input[name="category"][value="${currentFilters.category}"]`
  ).checked = true;

  document.querySelector(
    `input[name="sort"][value="${currentFilters.sort}"]`
  ).checked = true;
  document.getElementById("created-after").value =
    currentFilters.createdAfter || "";

  const searchValue = params.get("search") || "";
  document.querySelector(".search-field").value = searchValue;
}

document.addEventListener("DOMContentLoaded", function () {
  updateFiltersFromQueryParams();
  updateActiveFilters();
  applyFiltersToResults();
});

document.getElementById("apply-filters").addEventListener("click", () => {
  const contentType = document.querySelector(
    'input[name="content-type"]:checked'
  ).value;
  const category =
    document.querySelector('input[name="category"]:checked')?.value || "";
  const sort =
    document.querySelector('input[name="sort"]:checked')?.value || "";
  const createdAfter = document.getElementById("created-after").value;

  currentFilters = {
    contentType,
    category,
    sort,
    createdAfter,
  };

  updateActiveFilters();
  applyFiltersToResults();

  // close popup
  document.getElementById("filter-popup").classList.remove("active");

  // show success message

  updateQueryParamsFromFilters();
});

// reset filters
document.getElementById("reset-filters").addEventListener("click", () => {
  // reset form
  document.querySelector(
    'input[name="content-type"][value="collections"]'
  ).checked = true;
  document
    .querySelectorAll('input[name="category"]')
    .forEach((input) => (input.checked = false));
  document.querySelector('input[name="category"][value=""]').checked = true;
  document
    .querySelectorAll('input[name="sort"]')
    .forEach((input) => (input.checked = false));
  document.querySelector('input[name="sort"][value=""]').checked = true;
  document.getElementById("created-after").value = "";

  // Reset current filters
  currentFilters = {
    contentType: "collections",
    category: "",
    sort: "",
    createdAfter: "",
  };

  updateActiveFilters();

  updateQueryParamsFromFilters();
});

// search button click
document.querySelector(".search-btn").addEventListener("click", function () {
  updateQueryParamsFromFilters();
  applyFiltersToResults();
});

// enter in search field
document
  .querySelector(".search-field")
  .addEventListener("keydown", function (e) {
    if (e.key === "Enter") {
      updateQueryParamsFromFilters();
      applyFiltersToResults();
    }
  });

// filter state
let currentFilters = {
  contentType: "collections",
  category: "",
  sort: "",
  createdAfter: "",
};

// filter popup functionality
document.getElementById("open-filter-popup").addEventListener("click", () => {
  document.getElementById("filter-popup").classList.add("active");
});

document.getElementById("close-filter-popup").addEventListener("click", () => {
  document.getElementById("filter-popup").classList.remove("active");
});

document.getElementById("filter-popup").addEventListener("click", (e) => {
  if (e.target.id === "filter-popup") {
    document.getElementById("filter-popup").classList.remove("active");
  }
});

// apply filters
document.getElementById("apply-filters").addEventListener("click", () => {
  // get form data
  const contentType = document.querySelector(
    'input[name="content-type"]:checked'
  ).value;
  const category =
    document.querySelector('input[name="category"]:checked')?.value || "";
  const sort =
    document.querySelector('input[name="sort"]:checked')?.value || "";
  const createdAfter = document.getElementById("created-after").value;

  // update current filters
  currentFilters = {
    contentType,
    category,
    sort,
    createdAfter,
  };

  // update ui
  updateActiveFilters();
  applyFiltersToResults();

  // close popup
  document.getElementById("filter-popup").classList.remove("active");

  // show success message

  updateQueryParamsFromFilters();
});

// reset filters
document.getElementById("reset-filters").addEventListener("click", () => {
  // reset form
  document.querySelector(
    'input[name="content-type"][value="collections"]'
  ).checked = true;
  document
    .querySelectorAll('input[name="category"]')
    .forEach((input) => (input.checked = false));
  document.querySelector('input[name="category"][value=""]').checked = true;
  document
    .querySelectorAll('input[name="sort"]')
    .forEach((input) => (input.checked = false));
  document.querySelector('input[name="sort"][value=""]').checked = true;
  document.getElementById("created-after").value = "";

  // Reset current filters
  currentFilters = {
    contentType: "collections",
    category: "",
    sort: "",
    createdAfter: "",
  };

  updateActiveFilters();

  updateQueryParamsFromFilters();
});

// ppdate active filters display
function updateActiveFilters() {
  const activeFiltersContainer = document.getElementById("active-filters");
  const filterTagsContainer = document.getElementById("filter-tags");

  // clear existing tags
  filterTagsContainer.innerHTML = "";

  let hasActiveFilters = false;

  // add content type tag, always show if not default
  if (currentFilters.contentType !== "collections") {
    addFilterTag(
      "Tip",
      currentFilters.contentType === "objects" ? "Obiecte" : "Colecții"
    );
    hasActiveFilters = true;
  }

  // add category tag
  if (currentFilters.category) {
    const categoryNames = {
      monede: "Monede",
      viniluri: "Viniluri",
      timbre: "Timbre",
      tablouri: "Tablouri",
      others: "Altele",
    };
    addFilterTag("Categorie", categoryNames[currentFilters.category]);
    hasActiveFilters = true;
  }

  // add sort tag
  if (currentFilters.sort) {
    const sortNames = {
      popular: "Populare",
      trending: "În trend",
      valuable: "Valoroase",
      new: "Noi",
    };
    addFilterTag("Sortare", sortNames[currentFilters.sort]);
    hasActiveFilters = true;
  }

  // add date tag
  if (currentFilters.createdAfter) {
    addFilterTag("Created after", currentFilters.createdAfter);
    hasActiveFilters = true;
  }

  // show/hide active filters section
  activeFiltersContainer.style.display = hasActiveFilters ? "block" : "none";
}

function addFilterTag(label, value) {
  const filterTagsContainer = document.getElementById("filter-tags");
  const tag = document.createElement("span");
  tag.className = "filter-tag";
  tag.innerHTML = `${label}: ${value} <button onclick="removeFilterTag('${label}')">&times;</button>`;
  filterTagsContainer.appendChild(tag);
}

function removeFilterTag(label) {
  // remove specific filter based on label
  switch (label) {
    case "Tip":
      currentFilters.contentType = "collections";
      break;
    case "Categorie":
      currentFilters.category = "";
      break;
    case "Sortare":
      currentFilters.sort = "";
      break;
    case "Created after":
      currentFilters.createdAfter = "";
      break;
  }
  updateActiveFilters();
  applyFiltersToResults();
  updateQueryParamsFromFilters();
  setTimeout(() => {
    updateFiltersFromQueryParams();
  }, 1000);
}

function applyFiltersToResults() {
  const token = localStorage.getItem("token");
  let params = [];
  if (currentFilters.category)
    params.push(
      `colectie=${encodeURIComponent(currentFilters.category.toUpperCase())}`
    );
  if (currentFilters.createdAfter)
    params.push(
      `data=${currentFilters.createdAfter.split("-").reverse().join("-")}`
    );
  // sortare corectă pentru backend
  const sortMap = {
    popular: "populare",
    trending: "trend",
    valuable: "value",
    new: "date",
  };
  if (currentFilters.sort) {
    const backendSort = sortMap[currentFilters.sort] || currentFilters.sort;
    params.push(`sort=${encodeURIComponent(backendSort)}`);
  }
  // search bar
  const searchValue = document.querySelector(".search-field").value.trim();
  if (searchValue) params.push(`name=${encodeURIComponent(searchValue)}`);

  const query = params.length ? `?${params.join("&")}` : "";

  // slege endpoint ul in functie de tipul de continut
  const endpoint =
    currentFilters.contentType === "objects" ? "objects" : "all-collection";

  fetch(`http://localhost:1111/${endpoint}${query}`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })
    .then((res) => res.json())
    .then((items) => {
      if (endpoint === "objects") {
        document.querySelector(".posts-container").innerHTML = "";
        renderObjects(items);
      } else {
        document.querySelector(".posts-container").innerHTML = "";
        renderCollections(items);
      }
      // notificare
      let filterCount = 0;
      if (currentFilters.contentType !== "collections") filterCount++;
      if (currentFilters.category) filterCount++;
      if (currentFilters.sort) filterCount++;
      if (currentFilters.createdAfter) filterCount++;
      if (searchValue) filterCount++;
      // if (filterCount > 0) {
      //   showNotification(
      //     `${filterCount} filtre aplicate. Rezultatele au fost actualizate.`,
      //     "success"
      //   );
      // }
    })
    .catch(() => {
      document.querySelector(".posts-container").innerHTML =
        "<p>Eroare la încărcarea rezultatelor.</p>";
    });
}

function renderCollections(collections) {
  if (collections && collections.publicCollections) {
    collections = collections.publicCollections;
  }
  const container = document.querySelector(".posts-container");
  console.log("renderCollections called, collections:", collections);

  if (!collections || !collections.length) {
    container.innerHTML = "<p style='padding:2rem;'>Nicio colecție găsită.</p>";
    console.log("No collections to display.");
    return;
  }

  const emoticons = {
    MONEDE: "🪙",
    VINILURI: "🎵",
    TIMBRE: "📮",
    TABLOURI: "🖼️",
    OTHERS: "📦",
  };

  // log fiecare colectie individual
  collections.forEach((col, idx) => {
    console.log(`Colecție [${idx}]:`, col);
  });

  container.innerHTML = collections
    .map((col, idx) => {
      const html = `
  <div class="post-container">
    <div class="post-header">
      <div class="user-info">
        <span class="post-owner">@${col.username || "anonim"}</span>
      </div>
      <span class="post-tag moneda-tag">
        ${emoticons[col.tipColectie] || "📦"} ${(
        col.tipColectie || ""
      ).toLowerCase()}
      </span>
    </div>
    <hr class="post-delimiter" />
    <div class="post-body" style="display:flex;flex-direction:column;align-items:center;">
      <div style="font-size:4rem;line-height:1">${
        emoticons[col.tipColectie] || "📦"
      }</div>
      <p class="post-title">${col.name || "Fără titlu"}</p>
      <p class="post-description italic">${col.description || ""}</p>
      <div class="post-footer">
        <span class="post-likes post-statistic">❤️ ${col.likes ?? 0}</span>
        <span class="post-views post-statistic">👀 ${
          col.views ?? col.view ?? 0
        }</span>
        <span class="post-shares post-statistic">💰 ${col.value}$</span>
      </div>
    </div>
    <hr class="post-delimiter" />
    <div class="post-actions">
      <a href="page_view-collection-explore.html?id=${
        col.id
      }" class="view-button post-button">View</a>
    </div>
  </div>
  `;
      console.log(`Colecție [${idx}] HTML:`, html);
      return html;
    })
    .join("");

  console.log("container.innerHTML set:", container.innerHTML);
}

// function showNotification(message, type = "info") {
//   const notification = document.createElement("div");
//   notification.className = `notification notification-${type}`;
//   notification.textContent = message;

//   document.body.appendChild(notification);

//   // show notification
//   setTimeout(() => {
//     if (counter == 0) {
//       counter++;
//       notification.classList.add("show");
//     }
//   }, 100);

//   // hide and remove notification
//   setTimeout(() => {
//     notification.classList.remove("show");
//     setTimeout(() => {
//       document.body.removeChild(notification);
//       counter--;
//     }, 300);
//   }, 3000);
// }

// existing filter tabs functionality
document.querySelectorAll(".filter-tabs .filter-tab").forEach(function (btn) {
  btn.addEventListener("click", function () {
    document.querySelectorAll(".filter-tabs .filter-tab").forEach(function (b) {
      b.classList.remove("active");
    });
    this.classList.add("active");
  });
});

document
  .querySelectorAll(".filter-tabs-type .filter-tab")
  .forEach(function (btn) {
    btn.addEventListener("click", function () {
      document
        .querySelectorAll(".filter-tabs-type .filter-tab")
        .forEach(function (b) {
          b.classList.remove("active");
        });
      this.classList.add("active");
    });
  });

function renderObjects(objects) {
  console.log("[renderObjects] Called with:", objects);

  if (objects && Array.isArray(objects.objects)) {
    console.log("[renderObjects] Detected objects key, extracting array.");
    objects = objects.objects;
  } else if (objects && Array.isArray(objects.publicObjects)) {
    console.log(
      "[renderObjects] Detected publicObjects key, extracting array."
    );
    objects = objects.publicObjects;
  }
  const container = document.querySelector(".posts-container");
  if (!objects || !objects.length) {
    container.innerHTML = "<p style='padding:2rem;'>Niciun obiect găsit.</p>";
    console.log("[renderObjects] No objects to display.");
    return;
  }
  const emoticons = {
    MONEDE: "🪙",
    VINILURI: "🎵",
    TIMBRE: "📮",
    TABLOURI: "🖼️",
    OTHERS: "📦",
  };
  container.innerHTML = objects
    .map(
      (obj) => `
    <div class="post-container">
      <div class="post-header">
        <div class="user-info">
          <span class="post-owner">@${obj.username}</span>
        </div>
        <span class="post-tag moneda-tag">
          ${emoticons[obj.tipColectie] || "📦"} ${
        obj.tipColectie?.toLowerCase() || ""
      }
        </span>
      </div>
      <hr class="post-delimiter" />
      <div class="post-body" style="display:flex;flex-direction:column;align-items:center;">
        ${
          obj.image
            ? `<img src="data:image/png;base64,${obj.image}" alt="${obj.name}" class="post-image" />`
            : `<div style="font-size:4rem;line-height:1">${
                emoticons[obj.tipColectie] || "📦"
              }</div>`
        }
        <p class="post-title">${obj.name}</p>
        <p class="post-description italic">${obj.description || ""}</p>
        <div class="post-footer">
          <span class="post-likes post-statistic">❤️ ${obj.likes ?? 0}</span>
          <span class="post-views post-statistic">👀 ${
            obj.views ?? obj.view ?? 0
          }</span>
          <span class="post-shares post-statistic">💰 ${obj.value}$</span>
        </div>
      </div>
      <hr class="post-delimiter" />
      <div class="post-actions">
        <button type="button" class="like-button post-button" data-id="${
          obj.id
        }">Like</button>
        <a href="page_view-object-explore.html?id=${
          obj.id
        }" class="view-button post-button">View</a>
      </div>
    </div>
  `
    )
    .join("");

  // adauga handler pentru butoanele de like
  container.querySelectorAll(".like-button").forEach((btn) => {
    btn.addEventListener("click", async function (event) {
      event.preventDefault();
      alert(1);
      const objectId = this.getAttribute("data-id");
      const token = localStorage.getItem("token");
      // try {
      const res = await fetch(
        `http://localhost:1111/objects/${objectId}/like`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
    });
  });
}

// initialize filters display

function updateFiltersFromForm() {
  const contentType = document.querySelector(
    'input[name="content-type"]:checked'
  ).value;
  const category =
    document.querySelector('input[name="category"]:checked')?.value || "";
  const sort =
    document.querySelector('input[name="sort"]:checked')?.value || "";
  const createdAfter = document.getElementById("created-after").value;

  currentFilters = {
    contentType,
    category,
    sort,
    createdAfter,
  };
  updateActiveFilters();
}

document.getElementById("apply-filters").addEventListener("click", () => {
  updateFiltersFromForm(); // actualizează currentFilters din formular
  updateQueryParamsFromFilters();
  updateActiveFilters();
  applyFiltersToResults();

  // close popup
  document.getElementById("filter-popup").classList.remove("active");
});
