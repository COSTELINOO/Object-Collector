async function logout() {
  const token = localStorage.getItem("token");

  //send request to the server to log out

  localStorage.removeItem("token");
  window.location.href = "page_login.html";
}

document.getElementById("logout-button").addEventListener("click", logout);
