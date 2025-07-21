document
  .querySelector(".submit_button")
  .addEventListener("click", async function (e) {
    e.preventDefault();

    //iau din localStorage email ul salvat la pasul 1 si codul si parola din input

    const email = localStorage.getItem("email");
    const password = document.getElementById("password").value;
    const code = document.getElementById("code").value;
    const confirmPassword = document.getElementById("confirm-password").value;

    if (password.length < 7) {
      alert("Parola trebuie sa aiba cel putin 7 caractere!");
      return;
    }

    if (password !== confirmPassword) {
      alert("Parolele nu coincid!");
      return;
    }

    //fac post cu datele necesare
    const response = await fetch(
      "http://localhost:1111/auth/reset-password/" + code,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: email, password }),
      }
    );

    //daca raspunsul e ok, iau token ul si redirectionez catre pagina de explore

    const data = await response.json();
    if (response.ok) {
      alert("Reset password succesfull!");

      localStorage.setItem("token", data.token);
      localStorage.removeItem("email");
      window.location.href = "page_explore.html";
    } else {
      alert(data.error || "Login failed!");
    }
  });
