document
  .querySelector(".submit_button")
  .addEventListener("click", async function (e) {
    e.preventDefault();

    //iau toate elementele din input uri si validez datele
    const email = document.getElementById("email").value;
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirm-password").value;

    if (password.length < 7) {
      alert("Parola trebuie să aibă cel puțin 7 caractere!");
      return;
    }

    if (password !== confirmPassword) {
      alert("Parolele nu coincid!");
      return;
    }

    // post pentru cearea contului

    const response = await fetch("http://localhost:1111/auth/register", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, username, password }),
    });

    //iau token ul si redirectionez user ul

    try {
      const data = await response.json();
      if (response.ok) {
        localStorage.setItem("token", data.token);

        alert("Account created!");
        window.location.href = "page_explore.html";
      } else {
        alert(data.error || "Registration failed!");
      }
    } catch (error) {
      console.log(response);
      alert(response);
    }
  });
