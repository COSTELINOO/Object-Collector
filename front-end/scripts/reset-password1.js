document
  .querySelector(".submit_button")
  .addEventListener("click", async function (e) {
    e.preventDefault();
    // iau email ul din input si fac  post cu el
    const email = document.getElementById("email").value;

    const response = await fetch("http://localhost:1111/auth/reset-password", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: email }),
    });

    // iar raspunsul de  ok de la server si redirectionez catre pagina 2 de reset password
    const data = await response.json();
    if (response.ok) {
      alert("Pasul 1 de resetare complet!");

      // salvez email-ul in local storage pentru a l trimite la pasul 2
      localStorage.setItem("email", email);

      window.location.href = "page_reset-password2.html";
    } else {
      alert(data.error || "eroare la resetarea parolei!");
    }
  });
