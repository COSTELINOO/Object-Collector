document
  .querySelector(".submit_button")
  .addEventListener("click", async function (e) {
    e.preventDefault();

    //iar emai-ul si parola din input uri
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    // fac post pentru a trimite informatiile

    const response = await fetch("http://localhost:1111/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });

    // daca raspunsul e ok, primesc token il stochez in local storage si redirectionez catre pagina de explore

    const data = await response.json();
    if (response.ok) {
      localStorage.setItem("token", data.token);
      window.location.href = "page_explore.html";
    } else {
      alert(data.error || "Login failed!");
    }
  });
