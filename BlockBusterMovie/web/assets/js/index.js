function indexOnloadFunction(){
    checkSessionCartTickets();
    loadMovieData("recent");
    loadMovieData("newyear");
    loadMovieData("topimdbrated");

}

async function loadMovieData(type = "recent") {
    const popup = new Notification();
    const response = await fetch(`LoadHomeData?type=${type}`);
    if (response.ok) {
        const json = await response.json();
        if (json.status) {
            console.log(json);

            if (json.actors) {
                try {
                    window.lastFetchedActors = json.actors; 
                    loadSpotlightCelebrities();
                } catch (e) {
                    console.error("Failed to parse actors data:", e);
                }
            }

            if (type === "recent") {
                loadRecentAddedMovies(json);
            } else if (type === "newyear") {
                loadNewYearMovies(json);
            } else if (type === "topimdbrated") {
                loadTopIMDBRatedMovies(json); 
            } 

        } else {
            popup.error({ message: "Something went Wrong!" });
        }
    } else {
        popup.error({ message: "Something went Wrong!" });
    }
}

function loadRecentAddedMovies(json) {
    const container = document.getElementById("recent-movie-container");
    const cardTemplate = document.getElementById("recent-movie-card");

    container.innerHTML = ""; 
    
    if (!json.movies || !Array.isArray(json.movies)) {
        console.error("No movies found in the response:", json);
        return;
    }

    json.movies.forEach(movie => {  
        let card = cardTemplate.cloneNode(true);
        card.style.display = "block";

        const posterPath = "movie-posters/" + movie.id + "/poster.png";
        card.querySelector("#movie-img").src = posterPath;
        card.querySelector("#movie-img").alt = movie.title;

        card.querySelector("#movie-title").innerText = movie.title;
        card.querySelector("#movie-title").href = "moviesingle.html?id=" + movie.id;

        card.querySelector("#movie-rating").innerText = movie.imdb;
        card.querySelector("#movie-link").href = "moviesingle.html?id=" + movie.id;

        container.appendChild(card);
    });
}

function loadNewYearMovies(json) {
    const container = document.getElementById("newyear-movie-container");
    const cardTemplate = document.getElementById("newyear-movie-card");

    container.querySelectorAll(".slide-it").forEach(e => e.remove());

    if (!json.movies || !Array.isArray(json.movies)) {
        console.error("No new year movies found in the response:", json);
        return;
    }

    json.movies.forEach(movie => {
        const card = cardTemplate.cloneNode(true);
        card.style.display = "block";
        card.id = ""; 

        card.querySelector("#movie-img-new-released").src = "movie-posters/" + movie.id + "/poster.png";
        card.querySelector("#movie-img-new-released").alt = movie.title;

        card.querySelector("#movie-title-new-released").innerText = movie.title;
        card.querySelector("#movie-title-new-released").href = "moviesingle.html?id=" + movie.id;

        card.querySelector("#movie-rating-new-released").innerText = movie.imdb;
        card.querySelector("#movie-link-new-released").href = "moviesingle.html?id=" + movie.id;

        const slide = document.createElement("div");
        slide.className = "slide-it";
        slide.appendChild(card);

        container.appendChild(slide);
    });

}

function loadTopIMDBRatedMovies(json) {
    const container = document.getElementById("imdbrated-movie-container");
    const cardTemplate = document.getElementById("imdbrated-movie-card");

    // Clear old cards
    container.querySelectorAll(".slide-it").forEach(e => e.remove());

    if (!json.movies || !Array.isArray(json.movies)) {
        console.error("No top-rated movies found in the response:", json);
        return;
    }

    json.movies.forEach(movie => {
        const card = cardTemplate.cloneNode(true);
        card.style.display = "block";
        card.id = "";

        // Use class selectors instead of IDs
        card.querySelector("#movie-img-imdbrated").src = "movie-posters/" + movie.id + "/poster.png";
        card.querySelector("#movie-img-imdbrated").alt = movie.title;

        card.querySelector("#movie-title-imdbrated").innerText = movie.title;
        card.querySelector("#movie-title-imdbrated").href = "moviesingle.html?id=" + movie.id;

        card.querySelector("#movie-rating-imdbrated").innerText = movie.imdb;
        card.querySelector("#movie-link-imdbrated").href = "moviesingle.html?id=" + movie.id;


        const slide = document.createElement("div");
        slide.className = "slide-it";
        slide.appendChild(card);

        container.appendChild(slide);
    });
}

function loadSpotlightCelebrities() {
  const container = document.getElementById("spotlight-celebrities-container");
  const template = document.getElementById("spotlight-celeb-template");

  if (!window.lastFetchedActors || !Array.isArray(window.lastFetchedActors)) {
    console.error("No actors found in data");
    return;
  }

  container.querySelectorAll(".celeb-item:not(#spotlight-celeb-template)").forEach(e => e.remove());

  window.lastFetchedActors.forEach(actor => {
    const card = template.cloneNode(true);
    card.style.display = "flex";
    card.id = "";

    const imagePath = "actor-images/" + actor.id + "/actor.png";
    card.querySelector("#celeb-img").src = imagePath;
    card.querySelector("#celeb-img").alt = actor.name;

    card.querySelector("#celeb-name").innerText = actor.name;
    card.querySelector("#celeb-name").href = "celeb-profile.html?id=" + actor.id; 

    card.querySelector("#celeb-role").innerText = actor.role || "Actor"; 

    container.appendChild(card);
  });
}

async function checkSessionCartTickets() {
    try {
        const res = await fetch("CheckSessionCartTickets");
        if (res.ok) {
            const json = await res.json();
            if (json.status) {
                console.log(json.message);
                
                const popup = new Notification();
                popup.success({ message: json.message });
            } else {
                console.warn(json.message);
            }
        } else {
            console.error("Failed to call CheckSessionCartTickets API");
        }
    } catch (e) {
        console.error("Error in checkSessionCartTickets:", e);
    }
}


