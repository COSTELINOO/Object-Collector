document
  .querySelector(".submit_button")
  .addEventListener("click", async function (e) {
    e.preventDefault();

    // iau username si password din filed-uri
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    // trimite cerere catre server pentru login
    const response = await fetch("http://localhost:1111/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });

    // stochez endpoint-ul in local storage si redirectionez user ul
    const data = await response.json();
    if (response.ok) {
      localStorage.setItem("token", data.token);
      window.location.href = "page_explore.html";
    } else {
      alert(data.error || "Login failed!");
    }
  });
