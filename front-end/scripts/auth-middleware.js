async function checkAuth() {
  const publicPages = [
    "page_login.html",
    "page_login2.html",
    "page_register.html",
    "page_reset-password1.html",
    "page_reset-password2.html",
  ];
  let token = localStorage.getItem("token");

  const response = await fetch("http://localhost:1111/auth/info", {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  const data = await response.json();
  if (!response.ok) {
    localStorage.removeItem("token");
    token = localStorage.getItem("token");
  }

  if (
    !token &&
    !publicPages.includes(window.location.pathname.split("/").pop())
  ) {
    window.location.href = "page_login.html";
  }
}

checkAuth();
