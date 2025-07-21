document.addEventListener("DOMContentLoaded", async function () {
  try {
    const token = localStorage.getItem("token");
    const res = await fetch("http://localhost:1111/statistics/public/rss", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });

    const xmlText = await res.text();
    const parser = new DOMParser();
    const xml = parser.parseFromString(xmlText, "application/xml");

    // extrage toate item urile din rss
    const items = Array.from(xml.querySelectorAll("item")).map((item) => ({
      title: item.querySelector("title")?.textContent,
      description: item.querySelector("description")?.textContent,
    }));

    // creeaza un obiect pentru a stoca statisticile
    const stats = {};
    items.forEach(({ title, description }) => {
      stats[title] = description;
    });

    // populare statistici generale
    document.querySelectorAll(".general-statistics-card").forEach((card) => {
      const icon = card.querySelector(".general-statistics-icon")?.textContent;
      if (icon === "📁") {
        card.querySelector(".general-statistics-data").textContent =
          stats.totalCollections || "-";
      }
      if (icon === "🎯") {
        card.querySelector(".general-statistics-data").textContent =
          stats.totalObjects || "-";
      }
      if (icon === "💰") {
        card.querySelector(".general-statistics-data").textContent =
          stats.totalValue ? `$${stats.totalValue}` : "-";
      }
      if (icon === "📈") {
        card.querySelector(
          ".general-statistics-data"
        ).textContent = `+${stats.lastMonth}%`;
      }
    });

    // populare statistici pe tipuri de obiecte
    const typeMap = [
      { key: "procentTimbre", label: "Stamps" },
      { key: "procentVinil", label: "Vinil" },
      { key: "procentMonede", label: "Coins" },
      { key: "procentTablouri", label: "Art" },
      { key: "procentCustom", label: "Others" },
    ];
    document
      .querySelectorAll(".clasament-statistics-item")
      .forEach((item, idx) => {
        const key = typeMap[idx]?.key;
        if (key) {
          item.querySelector(".clasament-statistics-item-procent").textContent =
            stats[key] || "0";

          const colorMap = [
            "#ffe082",
            "#b3e5fc",
            "#c8e6c9",
            "#f8bbd0",
            "#c5afee",
          ];
          const baseColor = colorMap[idx] || "#eee";
          item.style.background = `linear-gradient(90deg, ${baseColor} ${stats[key]}%, #fff ${stats[key]}%)`;
        }
      });

    // --- COLECTII ---
    // Most liked collections
    document
      .querySelectorAll(
        ".collection-stats__sections .stats-card--likes .stats-item"
      )
      .forEach((item, idx) => {
        const i = idx + 1;
        console.log(`topLikeColName_${i}`, stats[`topLikeColName_${i}`]);
        item.querySelector(".stats-item__rank").textContent = i;
        item.querySelector(".stats-item__name").textContent =
          stats[`topLikeColName_${i}`] || "-";
        item.querySelector(".stats-item__value").textContent =
          stats[`topLikeColValue_${i}`] || "0";
        item.setAttribute(
          "href",
          stats[`topLikeColId_${i}`]
            ? `page_view-collection-explore.html?id=${
                stats[`topLikeColId_${i}`]
              }`
            : "#"
        );
      });

    //cele mai apreciate colectii

    document
      .querySelectorAll(
        ".collection-stats__sections .stats-card--views .stats-item"
      )
      .forEach((item, idx) => {
        const i = idx + 1;
        item.querySelector(".stats-item__rank").textContent = i;
        item.querySelector(".stats-item__name").textContent =
          stats[`topViewColName_${i}`] || "-";
        item.querySelector(".stats-item__value").textContent =
          stats[`topViewColValue_${i}`] || "0";
        item.setAttribute(
          "href",
          stats[`topViewColId_${i}`]
            ? `page_view-collection-explore.html?id=${
                stats[`topViewColId_${i}`]
              }`
            : "#"
        );
      });

    // cele mai scumpe colectii
    document
      .querySelectorAll(
        ".collection-stats__sections .stats-card--expensive .stats-item"
      )
      .forEach((item, idx) => {
        const i = idx + 1;
        item.querySelector(".stats-item__rank").textContent = i;
        item.querySelector(".stats-item__name").textContent =
          stats[`topPriceColName_${i}`] || "-";
        item.querySelector(".stats-item__value").textContent =
          stats[`topPriceColValue_${i}`] || "0";
        item.setAttribute(
          "href",
          stats[`topPriceColId_${i}`]
            ? `page_view-collection-explore.html?id=${
                stats[`topPriceColId_${i}`]
              }`
            : "#"
        );
      });

    // --- OBIECTE ---
    // cele mai apreciate obiecte
    document
      .querySelectorAll(".objects-stats .stats-card--likes .stats-item")
      .forEach((item, idx) => {
        console.log(stats[`topLikeObjName_${idx + 1}`]);
        const i = idx + 1;
        item.querySelector(".stats-item__rank").textContent = i;
        item.querySelector(".stats-item__name").textContent =
          stats[`topLikeObjName_${i}`] || "-";
        item.querySelector(".stats-item__value").textContent =
          stats[`topLikeObjValue_${i}`] || "0";
        item.setAttribute(
          "href",
          stats[`topLikeObjId_${i}`]
            ? `page_view-object-explore.html?id=${stats[`topLikeObjId_${i}`]}`
            : "#"
        );
      });

    // cele mai vizualizate obiecte
    document
      .querySelectorAll(".objects-stats .stats-card--views .stats-item")
      .forEach((item, idx) => {
        const i = idx + 1;
        console.log(stats[`topViewObjName_${i}`]);
        item.querySelector(".stats-item__rank").textContent = i;
        item.querySelector(".stats-item__name").textContent =
          stats[`topViewObjName_${i}`] || "-";
        item.querySelector(".stats-item__value").textContent =
          stats[`topViewObjValue_${i}`] || "0";
        item.setAttribute(
          "href",
          stats[`topViewObjId_${i}`]
            ? `page_view-object-explore.html?id=${stats[`topViewObjId_${i}`]}`
            : "#"
        );
      });

    //cele mai scumpe obiecte
    document
      .querySelectorAll(".objects-stats .stats-card--expensive .stats-item")
      .forEach((item, idx) => {
        const i = idx + 1;
        item.querySelector(".stats-item__rank").textContent = i;
        item.querySelector(".stats-item__name").textContent =
          stats[`topPriceObjName_${i}`] || "-";
        item.querySelector(".stats-item__value").textContent =
          stats[`topPriceObjValue_${i}`] || "0";
        item.setAttribute(
          "href",
          stats[`topPriceObjId_${i}`]
            ? `page_view-object-explore.html?id=${stats[`topPriceObjId_${i}`]}`
            : "#"
        );
      });
  } catch (err) {
    console.error("Eroare la încărcarea statisticilor RSS:", err);
  }
});

document
  .querySelectorAll(".button.small-button.green-button.export-csv")
  .forEach((btn) => {
    btn.addEventListener("click", async function () {
      const token = localStorage.getItem("token");
      try {
        const res = await fetch("http://localhost:1111/statistics/public/csv", {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        if (!res.ok) {
          alert("Eroare la descărcarea CSV!");
          return;
        }
        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "statistics.csv";
        document.body.appendChild(a);
        a.click();
      } catch (err) {
        alert("Eroare la descărcare!");
      }
    });
  });

document
  .querySelectorAll(".button.small-button.green-button.export-pdf")
  .forEach((btn) => {
    btn.addEventListener("click", async function () {
      const token = localStorage.getItem("token");
      try {
        const res = await fetch("http://localhost:1111/statistics/public/pdf", {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
        if (!res.ok) {
          alert("eroare la descarcare pdf!");
          return;
        }
        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "statistics.pdf";
        document.body.appendChild(a);
        a.click();
      } catch (err) {
        alert("erare la descarcare!");
      }
    });
  });
