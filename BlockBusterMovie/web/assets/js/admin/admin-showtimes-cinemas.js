function loadData(){
    getCityData();
    getCinemaData();
    getMovieData();
    document.getElementById("show-cinema").addEventListener("change", function () {
        getHallsByCinema(this.value);
    });
}

async function getCityData(){
    const response = await fetch("CityData");
    
    if (response.ok) {
        const json = await response.json();
        const citySelect = document.getElementById("city");
        json.forEach(city => {
            let option = document.createElement("option");
            option.innerHTML = city.name;
            option.value = city.id;
            citySelect.appendChild(option);
        });
    }
}

async function addCinema() {
    const popup = new Notification();

    const nameInput = document.getElementById("cinema-name");
    const citySelect = document.getElementById("city");

    const name = nameInput.value.trim();
    const cityId = parseInt(citySelect.value);

    if (name === "" || cityId === 0) {
        popup.error({ message: "Please enter a cinema name and select a city." });
        return;
    }

    try {
        const formData = new FormData();
        formData.append("name", name);
        formData.append("cityId", cityId);

        const res = await fetch("SaveCinema", {
            method: "POST",
            body: formData
        });

        const json = await res.json();

        if (json.status) {
            popup.success({ message: json.message });
            nameInput.value = "";
            citySelect.value = "0";
        } else {
            popup.error({ message: json.message });
        }
    } catch (err) {
        popup.error({ message: "Error: " + err.message });
    }
}

async function getCinemaData() {
    try {
        const res = await fetch("CinemaData");
        if (res.ok) {
            const cinemas = await res.json();
            const cinemaSelect = [document.getElementById("cinema"), document.getElementById("show-cinema")];
            cinemas.forEach(cinema => {
                cinemaSelect.forEach(select => {
                    const option = document.createElement("option");
                    option.value = cinema.id;
                    option.innerHTML = cinema.name;
                    select.appendChild(option);
                });
            });
        }
    } catch (err) {
        console.error("Error loading cinemas:", err);
    }
}

async function addHall() {
    const popup = new Notification();

    const name = document.getElementById("hall-name").value.trim();
    const seats = document.getElementById("seats").value.trim();
    const cinemaId = document.getElementById("cinema").value;

    try {
        const formData = new FormData();
        formData.append("name", name);
        formData.append("seats", seats);
        formData.append("cinemaId", cinemaId);

        const res = await fetch("SaveHall", {
            method: "POST",
            body: formData
        });

        const json = await res.json();
        if (json.status) {
            popup.success({ message: json.message });
            document.getElementById("hall-name").value = "";
            document.getElementById("seats").value = "";
            document.getElementById("cinema").value = "0";
        } else {
            popup.error({ message: json.message });
        }
    } catch (err) {
        popup.error({ message: "Error: " + err.message });
    }
}

async function getMovieData() {
    try {
        const res = await fetch("MovieData");
        if (res.ok) {
            const movies = await res.json();
            const movieSelect = document.getElementById("show-movie");
            movies.forEach(movie => {
                let option = document.createElement("option");
                option.value = movie.id;
                option.innerHTML = movie.title;
                movieSelect.appendChild(option);
            });
        }
    } catch (err) {
        console.error("Error loading movies:", err);
    }
}

async function getHallsByCinema(cinemaId) {
    const hallSelect = document.getElementById("show-hall");
    hallSelect.innerHTML = '<option value="0">Select</option>';

    if (cinemaId === "0") return; 

    try {
        const res = await fetch("HallByCinemaData?cinemaId=" + cinemaId);
        if (res.ok) {
            const halls = await res.json();
            halls.forEach(hall => {
                let option = document.createElement("option");
                option.value = hall.id;
                option.innerHTML = hall.name;
                hallSelect.appendChild(option);
            });
        }
    } catch (err) {
        console.error("Error loading halls:", err);
    }
}

async function addShowTime() {
    const popup = new Notification();

    const movieId = document.getElementById("show-movie").value;
    const cinemaId = document.getElementById("show-cinema").value;
    const hallId = document.getElementById("show-hall").value;
    const showDate = document.getElementById("show-date").value;
    const showTime = document.getElementById("show-time").value;
    const price = document.querySelector("#show-price").value.trim();

    try {
        const formData = new FormData();
        formData.append("movieId", movieId);
        formData.append("cinemaId", cinemaId);
        formData.append("hallId", hallId);
        formData.append("showDate", showDate);
        formData.append("showTime", showTime);
        formData.append("price", price);

        const res = await fetch("SaveShowTime", {
            method: "POST",
            body: formData
        });

        const json = await res.json();
        if (json.status) {
            popup.success({ message: json.message });
            document.getElementById("show-movie").value = "0";
            document.getElementById("show-cinema").value = "0";
            document.getElementById("show-hall").innerHTML = '<option value="0">Select</option>';
            document.getElementById("show-date").value = "";
            document.getElementById("show-time").value = "";
            document.querySelector("#show-price").value = "";
        } else {
            popup.error({ message: json.message });
        }
    } catch (err) {
        popup.error({ message: "Error: " + err.message });
    }
}