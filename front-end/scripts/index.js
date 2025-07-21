import API from "./fetch.js";

async function getImages() {
  try {
    const response = await API.GET("/v1/images/0XYvRd7oD");
    const p = document.getElementById("card1_image");
    if (p) {
      p.innerHTML = response.id;

      const users = ["nume1", "nume2", "nume3"];
      const wrapper = document.getElementById("users");
      console.log(wrapper);
      users.forEach((user) => {
        const el = document.createElement("p");
        el.innerText = user;
        wrapper.appendChild(el);
      });
    }
  } catch (error) {
    console.log(error);
  }
}

getImages();
