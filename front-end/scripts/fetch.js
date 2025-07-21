class API {
  constructor(URL) {
    this.URL = URL;
  }

  async GET(params = "") {
    try {
      const response = await fetch(this.URL + params);
      if (response.status == 200) {
        const data = await response.json();
        return data;
      }
      throw new Error("Response status: error");
    } catch (error) {
      console.log(error);
    }
  }
}

const instance = new API("https://api.thecatapi.com");
export default instance;

// async function init() {
//   const instance = new API("https://api.thecatapi.com");
//   const response = await instance.GET("/v1/images/0XYvRd7oD");

//   // const response = await fetch("https://api.thecatapi.com/v1/images/0XYvRd7oD");
//   // const d = await response.json();
//   // console.log(d.id);
// }

// init();

// // export default instance;
