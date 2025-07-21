// astept sa se incarce tot html ul
document.addEventListener("DOMContentLoaded", async function () {
  const params = new URLSearchParams(window.location.search);
  const collectionId = params.get("id");
  if (!collectionId) {
    alert("Lipseste id-ul nu l ai luat cum trebuie!");
    return;
  }

  // iau token din local storage
  try {
    const token = localStorage.getItem("token");
    const res = await fetch(
      `http://localhost:1111/user-collection/${collectionId}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );

    if (!res.ok) {
      alert("Eroare la incarcarea colectiei!");
      return;
    }

    // iau raspunsul de la back
    const data = await res.json();
    const collection = data?.collection;
    if (!collection) {
      alert("Colectia nu a fost gasita!");
      return;
    }

    document.getElementById("collection-name").value = collection.nume || "";
    document.getElementById("collection-visibility").value =
      collection.visibility ? "true" : "false";
  } catch (err) {
    alert("Eroare la conectarea cu serverul!");
  }
});

// form de editare colectie
document
  .getElementById("edit-collection-form")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    // iau id ul colectiei
    const params = new URLSearchParams(window.location.search);
    const collectionId = params.get("id");
    if (!collectionId) {
      alert("Lipseste id!");
      return;
    }

    const collectionName = document.getElementById("collection-name").value;
    const collectionVisibility =
      document.getElementById("collection-visibility").value === "true";

    // construieste payload ul de trimis catre server
    let payload = {
      nume: collectionName,
      visibility: collectionVisibility,
    };

    //fetch pentru update

    try {
      const token = localStorage.getItem("token");
      const res = await fetch(
        `http://localhost:1111/user-collection/${collectionId}`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(payload),
        }
      );

      if (!res.ok) {
        const err = await res.text();
        alert("eroare la actulaizare: " + err);
        return;
      }
      alert("Colectia a fost edita cu succes!");
      window.location.reload(); // reload pentru a vedea schimbarile
    } catch (err) {
      alert("Eroare la conectarea cu serverul!");
    }
  });

// delete collection
document
  .getElementById("delete-collection-btn")
  .addEventListener("click", async function () {
    if (confirm("Esti sigur ca vrei sa strergi aceasta colectie?")) {
      // ia id-ul colectiei din url
      const params = new URLSearchParams(window.location.search);
      const collectionId = params.get("id");
      if (!collectionId) {
        alert("Lipseste id ul pentru fetch!");
        return;
      }
      try {
        const token = localStorage.getItem("token");
        const res = await fetch(
          `http://localhost:1111/user-collection/${collectionId}`,
          {
            method: "DELETE",
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
        if (!res.ok) {
          const err = await res.text();
          alert("Eroare la stergerea colectiei " + err);
          return;
        }
        alert("Colectie stearsa cu succes!");
        window.location.href = "page_collections.html"; // redirectionare la pagina de colectii
      } catch (err) {
        alert("Eroare la conectarea cu serverul!");
      }
    }
  });
