//fac get pentru a lua toate informatiile despre user
async function getUserInfo() {
  const token = localStorage.getItem("token"); // folosesc token ul salval la login in local storage

  const response = await fetch("http://localhost:1111/auth/info", {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  // iau raspunsul de la server si daca e ok actualizez informatiile in pagina
  const data = await response.json();
  if (response.ok) {
    document.querySelector(".profile-name").textContent = data.username;
    document.querySelector(".profile-email").textContent = data.email;
    document.querySelector(".filed-username").value = data.username;
    document.querySelector(".filed-email").value = data.email;
    document.querySelector(".collections-count").textContent =
      data.countCollections;
    document.querySelector(".objects-count").textContent = data.countObjects;
    document.querySelector(".total-value").textContent = data.value + "$";
    document.querySelector(".views-count").textContent = data.views;
    document.querySelector(".likes-count").textContent = data.likes;
    document.querySelector(
      ".profile-joined"
    ).textContent = `Member since ${new Date(
      data.created
    ).toLocaleDateString()}`;

    if (data.image) {
      document.querySelector(
        ".avatar-circle"
      ).innerHTML = `<img src="data:image/png;base64,${data.image}" alt="Avatar" style="width: 100px; height: 100px; border-radius: 50%; object-fit: cover;">`;
    } else {
      document.querySelector(".avatar-circle").textContent = "👤";
    }
  } else {
    alert(data.error || "eroare la obtinerea informatiilor");
  }
}

getUserInfo();

// functii de validare username
function validateUsername(username) {
  if (username.length < 3) {
    alert("Username-ul trebuie sa aiba o lungime de cel putin 3 caractere!");
    return false;
  }

  if (username.length > 20) {
    alert("Username-ul nu paote sa aiba mai mult de 20 de caractere!");
    return false;
  }

  if (!/^[a-zA-Z0-9_]+$/.test(username)) {
    alert("Username ul poate contine doar litere sau cifre");
    return false;
  }

  return true;
}

//funtie pentru validare email

function validateEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    alert("Email ul nu este valid!");
    return false;
  }

  return true;
}

//functie pentru validare parola

function validatePassword(password) {
  if (password.length < 7) {
    alert("Parola trebuie sa contina cel putin 7 caractere!");
    return false;
  }

  return true;
}

// avatar functionality

function openAvatarModal() {
  document.getElementById("avatar-modal").style.display = "flex";
}

//resetare avatar modal
function closeAvatarModal() {
  document.getElementById("avatar-modal").style.display = "none";
  document.getElementById("avatar-upload").value = "";
  document.getElementById("image-preview").style.display = "none";
}

async function saveAvatar() {
  const fileInput = document.getElementById("avatar-upload");
  if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
    alert("Nu a fost gasit niciun fisier in cerere");
    return;
  }
  const file = fileInput.files[0];
  if (!file.type.startsWith("image/")) {
    alert("Te rugam sa selectezi o imagine");
    return;
  }

  const formData = new FormData();
  formData.append("file", file);

  const token = localStorage.getItem("token");
  const response = await fetch("http://localhost:1111/auth/change-picture", {
    method: "PUT",
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: formData,
  });

  const data = await response.json();
  if (response.ok) {
    closeAvatarModal();
    getUserInfo();
  } else {
    alert(data.error || "eroare la in schimbarea avatarului!");
  }
}

// dupa terminarea incarcarii html ului fac callback pentru a adauga evenimentele necesare
document.addEventListener("DOMContentLoaded", function () {
  // event pentru a deschide pop ul de schimbare a avatarului
  document
    .querySelector(".change-avatar-btn")
    .addEventListener("click", openAvatarModal);

  // event peentru a inchide pop ul de schimbare a avatarului
  document
    .querySelector(".close-modal")
    .addEventListener("click", closeAvatarModal);

  //event pentru a face upload la avatar
  document
    .getElementById("avatar-upload")
    .addEventListener("change", function (e) {
      const file = e.target.files[0];
      if (file) {
        if (!file.type.startsWith("image/")) {
          alert("Please select an image file");
          return;
        }

        document.getElementById("image-preview").textContent =
          e.target.files[0].name;
        document.getElementById("image-preview").style.display = "block";
      }
    });

  // event pentru a salva avatarul
  document
    .querySelector(".avatar-modal .submit_button")
    .addEventListener("click", saveAvatar);

  // event formular schimbare username
  document
    .getElementById("username-form")
    .addEventListener("submit", async function (e) {
      e.preventDefault();

      // iau datele din inputuri
      const username = document.getElementById("new-username").value;
      const password = document.getElementById("username-password").value;

      // daca exista erori nu continui in functie si doar afisez alert urile
      if (
        validateUsername(username) == false ||
        validatePassword(password) == false
      ) {
        return;
      }

      // iau token ul din local storage
      const token = localStorage.getItem("token");
      const response = await fetch(
        "http://localhost:1111/auth/change-username",
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },

          body: JSON.stringify({ newUsername: username, password }),
        }
      );

      // in functie de raspunsul de la server afisez mesajul corespunzator

      const data = await response.json();
      if (response.ok) {
        alert("Username a fost modificat cu succes!");

        localStorage.setItem("token", data.token);
        getUserInfo(); // facem update la informatii
        document.getElementById("username-password").value = "";
      } else {
        alert(data.error || "Username ul nu a putut fi schimbat!");
      }
    });

  // email form
  document
    .getElementById("email-form")
    .addEventListener("submit", async function (e) {
      e.preventDefault();

      //iau datele din input uri
      const email = document.getElementById("new-email").value;
      const password = document.getElementById("email-password").value;

      if (
        validateEmail(email) == false ||
        validatePassword(password) == false
      ) {
        return;
      }

      const token = localStorage.getItem("token");
      const response = await fetch("http://localhost:1111/auth/change-email", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },

        body: JSON.stringify({ newEmail: email, password }),
      });

      // in functie de raspunsul de la server afisez mesajul corespunzator

      const data = await response.json();
      if (response.ok) {
        alert("Email-ul a fost modificat cu succes!");
        localStorage.setItem("token", data.token);
        getUserInfo(); // facem update la informatii
        document.getElementById("username-password").value = "";

        window.location.reload(); // reload page to update email display
      } else {
        alert(data.error || "Email ul nu a putut fi schimbat!");
      }
    });

  // password form
  document
    .getElementById("password-form")
    .addEventListener("submit", async function (e) {
      e.preventDefault();
      const currentPassword = document.getElementById("current-password").value;
      const newPassword = document.getElementById("new-password").value;
      const confirmPassword = document.getElementById("confirm-password").value;

      if (
        validatePassword(newPassword) == false ||
        validatePassword(currentPassword) == false
      ) {
        return;
      }

      if (newPassword !== confirmPassword) {
        alert("Passwords do not match");
        return;
      }

      const token = localStorage.getItem("token");
      const response = await fetch(
        "http://localhost:1111/auth/change-password",
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },

          body: JSON.stringify({
            newPassword,
            password: currentPassword,
          }),
        }
      );

      const data = await response.json();
      if (response.ok) {
        alert("Password updated successfully!");
        localStorage.setItem("token", data.token);
        getUserInfo(); // facem update la informatii
        document.getElementById("current-password").value = "";
        document.getElementById("new-password").value = "";
        document.getElementById("confirm-password").value = "";
      } else {
        alert(data.error || "password change failed!");
      }
    });

  // event pentru sergerea profilului
  document
    .getElementById("danger-form")
    .addEventListener("submit", async function (e) {
      e.preventDefault();
      if (
        confirm(
          "Esti sigur ca vrei sa-ti stergi contul? Actiunea este ireveribila."
        )
      ) {
        const password = document.getElementById("danger-password").value;

        const token = localStorage.getItem("token");
        const response = await fetch("http://localhost:1111/auth/delete", {
          method: "DELETE",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },

          body: JSON.stringify({
            password: password,
          }),
        });

        //in functie de raspunsul de la server afisez informatia catre utilizator

        const data = await response.json();
        if (response.ok) {
          localStorage.removeItem("token");
          window.location.href = "page_login.html";
          alert("Cont sters cu succes!");
        } else {
          alert(data.error || "stergere cont nu a reusit!");
        }
      }
    });
});

//event pentru descarcarea csv-ului

document
  .querySelector(".green-button.export-csv")
  .addEventListener("click", async function () {
    const token = localStorage.getItem("token");
    try {
      const res = await fetch("http://localhost:1111/statistics/personal/csv", {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      if (!res.ok) {
        alert("Eroare la descărcarea CSV!");
        return;
      }

      //minipularea datelor bineare
      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = "personal-statistics.csv";
      document.body.appendChild(a); // adaga link-ul la body
      a.click();
    } catch (err) {
      alert("Eroare la descarcare csv!");
    }
  });

// la click pe export pdf fac fetch catre server
document
  .querySelector(".export-pdf")
  .addEventListener("click", async function () {
    const token = localStorage.getItem("token"); // iau token din local storage
    try {
      const res = await fetch("http://localhost:1111/statistics/personal/pdf", {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      // daca raspunsul nu e ok afisez eroare
      if (!res.ok) {
        alert("Eroare la descarcarea PDF!");
        return;
      }

      // daca raspunsul e ok iau blob-ul si creez un link pentru download
      const blob = await res.blob();
      const url = window.URL.createObjectURL(blob);
      console.log("PDF URL:", url); // pentru debugging
      const a = document.createElement("a");
      a.href = url;
      a.download = "public-statistics.pdf";
      document.body.appendChild(a);
      a.click();
    } catch (err) {
      alert("Eroare la descarcare!");
    }
  });
