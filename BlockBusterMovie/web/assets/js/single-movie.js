async function loadData(){
    
    const searchParams = new URLSearchParams(window.location.search);
    if (searchParams.has("id")) {
        const movieId = searchParams.get("id");   
        const response = await fetch("LoadSingleMovie?id="+movieId);
        
        if (response.ok) {
            const json = await response.json();
            console.log("JSON data received:", json);
            if(json.status){
                console.log("Movie object:", json.movie);
                
                document.getElementById("image1").src = "movie-posters\\"+json.movie.id+"\\poster.png";
                document.getElementById("movie-title").childNodes[0].nodeValue = json.movie.title + " ";              
                document.getElementById("year").innerHTML = json.movie.releaseYear;
                document.getElementById("year-side-panel").innerHTML = json.movie.releaseYear;
                document.getElementById("trailer").href = json.movie.trailerUrl;
                document.getElementById("imdb").innerHTML = json.movie.imdbRating;
                document.getElementById("rt_rating").innerHTML = json.movie.rtRating;
                document.getElementById("description").innerHTML = json.movie.description;
                document.getElementById("runtime-side-panel").innerHTML = json.movie.runtime;
                document.getElementById("cast-movie-title").innerHTML = json.movie.title;
                document.getElementById("similar-movie-title").innerHTML = json.movie.title;
                document.getElementById("buy-movie-title").innerHTML = json.movie.title;
              
                //directors
                if (json.movie.directors && json.movie.directors.length > 0) {
                    const directorEl = document.getElementById("director-side-panel");
                    const directorNames = json.movie.directors.map(d => d.name).join(", ");
                    if (directorEl) {
                        directorEl.innerText = directorNames;
                    }
                    const directorNameEl = document.getElementById("director-name");
                    if (directorNameEl) {
                        directorNameEl.innerText = directorNames;
                    }
                } else {
                    const directorNameEl = document.getElementById("director-name");
                    if (directorNameEl) {
                        directorNameEl.innerText = "N/A";
                    }
                    const directorEl = document.getElementById("director-side-panel");
                    if (directorEl) {
                        directorEl.innerText = "N/A";
                    }
                }
                
                const directorsPanel = document.getElementById("directors-panel");
                directorsPanel.innerHTML = ""; 

                if (json.movie.directors && json.movie.directors.length > 0) {
                    json.movie.directors.forEach((director, index) => {
                        const castItDiv = document.createElement("div");
                        castItDiv.className = "cast-it";
                        const castLeftDiv = document.createElement("div");
                        castLeftDiv.className = "cast-left";
                        
                        const img = document.createElement("img");
                        img.id = `director-image-${index}`;
                        img.src = `director-images\\${director.id}\\director.png`;
                        img.alt = "";
                        img.className = "fixed-cast-image"; 

                        const nameLink = document.createElement("a");
                        nameLink.href = "#";
                        nameLink.id = `director-name-${index}`;
                        nameLink.innerText = director.name;

                        castLeftDiv.appendChild(img);
                        castLeftDiv.appendChild(nameLink);

                        const roleP = document.createElement("p");
                        roleP.innerText = "... Director";

                        castItDiv.appendChild(castLeftDiv);
                        castItDiv.appendChild(roleP);
                        directorsPanel.appendChild(castItDiv);
                    });
                } else {
                    directorsPanel.innerHTML = "<p>No directors available</p>";
                }

                
                //actors
                if (json.movie.actors && json.movie.actors.length > 0) {
                    const starsEl = document.getElementById("actor-side-panel");
                    starsEl.innerHTML = "";
                    json.movie.actors.forEach((actor, index) => {
                        const a = document.createElement("a");
                        a.href = "#"; 
                        a.textContent = actor.name;
                        starsEl.appendChild(a);
                        if (index < json.movie.actors.length - 1) {
                            starsEl.appendChild(document.createTextNode(", "));
                        }
                    });
                } else {
                    const starsEl = document.getElementById("actor-side-panel");
                    if (starsEl) {
                        starsEl.innerText = "N/A";
                    }
                }
                
                const actorsPanel = document.getElementById("actors-panel");
                actorsPanel.innerHTML = "";

                if (json.movie.actors && json.movie.actors.length > 0) {
                    json.movie.actors.forEach((actor, index) => {
                        const castItDiv = document.createElement("div");
                        castItDiv.className = "cast-it";
                        const castLeftDiv = document.createElement("div");
                        castLeftDiv.className = "cast-left";

                        const img = document.createElement("img");
                        img.id = `actor-image-${index}`;
                        img.src = `actor-images\\${actor.id}\\actor.png`;
                        img.alt = "";
                        img.className = "fixed-cast-image"; 

                        const nameLink = document.createElement("a");
                        nameLink.href = "#";
                        nameLink.id = `actor-name-${index}`;
                        nameLink.innerText = actor.name;

                        castLeftDiv.appendChild(img);
                        castLeftDiv.appendChild(nameLink);

                        const roleP = document.createElement("p");
                        roleP.innerText = "... Actor";

                        castItDiv.appendChild(castLeftDiv);
                        castItDiv.appendChild(roleP);
                        actorsPanel.appendChild(castItDiv);
                    });
                } else {
                    actorsPanel.innerHTML = "<p>No actors available</p>";
                }

                
                //genre
                if (json.movie.genres && json.movie.genres.length > 0) {
                    const genreEl = document.getElementById("genre-side-panel");
                    genreEl.innerHTML = "";
                    json.movie.genres.forEach((genre, index) => {
                        const a = document.createElement("a");
                        a.href = "#"; 
                        a.textContent = genre.name;
                        genreEl.appendChild(a);
                        if (index < json.movie.genres.length - 1) {
                            genreEl.appendChild(document.createTextNode(", "));
                        }
                    });
                } else {
                    const genreEl = document.getElementById("genre-side-panel");
                    if (genreEl) {
                        genreEl.innerText = "N/A";
                    }
                }

                //mpaa
                const mpaaEl = document.getElementById("mpaa-side-panel");
                if (json.movie.mpaaRating && json.movie.mpaaRating.value) {
                    mpaaEl.innerText = json.movie.mpaaRating.value;
                } else {
                    mpaaEl.innerText = "N/A";
                }
     
                //keyword
                const keywordsEl = document.getElementById("keyword-side-panel");
                keywordsEl.innerHTML = "";

                const keywords = json.movie.plotKeywords; // 🔁 FIXED

                if (Array.isArray(keywords) && keywords.length > 0) {
                    keywords.forEach(keywordObj => {
                        const span = document.createElement("span");
                        span.className = "time";
                        const link = document.createElement("a");
                        link.href = "#";
                        link.innerText = keywordObj.keyword;
                        span.appendChild(link);
                        keywordsEl.appendChild(span);
                    });
                } else {
                    keywordsEl.innerHTML = "<span>No keywords available</span>";
                }

//similar products
                let similarMovieMain = document.getElementById("similar-movie-main");
                let movieTemplate = document.getElementById("similar-movie-template"); 

                similarMovieMain.innerHTML = ""; 

                if (Array.isArray(json.similarMovies)) {

                    json.similarMovies.forEach(movie => {
                        let movieClone = movieTemplate.cloneNode(true);
                        movieClone.style.display = "block"; 
                        movieClone.removeAttribute("id");

                        // Image
                        const imgEl = movieClone.querySelector("img");
                        imgEl.src = "movie-posters/" + movie.id + "/poster.png";
                        imgEl.alt = movie.title;
                        imgEl.classList.add("fixed-poster-image");


                        movieClone.querySelector("img").alt = movie.title;

                        // Title and year
                        const titleLink = movieClone.querySelector("h6 > a");
                        if (titleLink) {
                            titleLink.href = "moviesingle.html?id=" + movie.id;
                            titleLink.innerHTML = `${movie.title} <span>(${movie.releaseYear})</span>`;
                        }

                        // IMDB Rating
                        const rateSpan = movieClone.querySelector(".rate span");
                        if (rateSpan) {
                            rateSpan.innerHTML = movie.imdbRating ?? "N/A";
                        }

                        // Description
                        const describeP = movieClone.querySelector(".describe");
                        if (describeP) {
                            describeP.innerHTML = movie.description ?? "No description available";
                        }

                        // Runtime, MPAA rating, Release
                        const runTimeP = movieClone.querySelector(".run-time");
                        if (runTimeP) {
                            const mpaaValue = movie.mpaaRating?.value ?? "N/A";
                            runTimeP.innerHTML = 
                                `Run Time: ${movie.runtime ?? "N/A"} mins &nbsp; . &nbsp; <span>MMPA: ${mpaaValue}</span> &nbsp; . &nbsp; <span>Release: ${movie.releaseYear ?? "N/A"}</span>`;
                        }

                       const directorP = movieClone.querySelector(".director");
                        if (directorP) {
                            const directorLinks = Array.isArray(movie.directors) 
                                ? movie.directors.map(d => `<a href="#">${d.name}</a>`).join(", ")
                                : "N/A";
                            directorP.innerHTML = `Director: ${directorLinks}`;
                        }

                        const starsP = movieClone.querySelector(".stars");
                        if (starsP) {
                            const actorLinks = Array.isArray(movie.actors) 
                                ? movie.actors.map(a => `<a href="#">${a.name}</a>`).join(", ") 
                                : "N/A";
                            starsP.innerHTML = `Stars: ${actorLinks}`;
                        }

                        similarMovieMain.appendChild(movieClone);
                    });

                } else {
                    console.warn("json.similarMovies is not available or not an array:", json.similarMovies);
                }
                
    
            }
        }
    }
}


async function loadTicketData() {
    const popup = new Notification();
    const searchParams = new URLSearchParams(window.location.search);
    const movieId = searchParams.get("id");

    if (!movieId) {
        popup.error({ message: "No movie ID found" });
        return;
    }

    const cinemaSelect = document.getElementById("cinema");
    const hallSelect = document.getElementById("hall");
    const showtimeSelect = document.getElementById("show-time");

    try {
        const cinemaRes = await fetch("LoadTicketData?action=cinemas");
        if (cinemaRes.ok) {
            const json = await cinemaRes.json();
            if (json.cinemas?.length > 0) {
                json.cinemas.forEach(c => {
                    const opt = document.createElement("option");
                    opt.value = c.id;
                    opt.textContent = c.name;
                    cinemaSelect.appendChild(opt);
                });
                
                cinemaSelect.selectedIndex = 0;
                cinemaSelect.dispatchEvent(new Event("change"));
            } else {
                console.error("Cinemas not loaded correctly:", json);
            }
        }
    } catch (e) {
        console.error("Error fetching cinemas:", e);
    }

    async function loadHallsByCinema(cinemaId) {
        hallSelect.innerHTML = "";
        if (!cinemaId) return;

        try {
            const res = await fetch(`LoadTicketData?action=halls&cinemaId=${cinemaId}`);
            if (res.ok) {
                const json = await res.json();
                if (json.halls?.length > 0) {
                    json.halls.forEach(h => {
                        const opt = document.createElement("option");
                        opt.value = h.id;
                        opt.textContent = h.name;
                        hallSelect.appendChild(opt);
                    });
                } else {
                    console.log("No halls found:", json);
                }
            }
        } catch (e) {
            console.log("Error loading halls:", e);
        }
    }

    cinemaSelect.addEventListener("change", function () {
        const cinemaId = this.value;
        loadHallsByCinema(cinemaId);
        hallSelect.innerHTML = "";
        showtimeSelect.innerHTML = "";
    });

    async function loadShowtimes(cinemaId, hallId, date, movieId) {
        showtimeSelect.innerHTML = "";
        if (!cinemaId || !hallId || !date || !movieId) return;

        try {
            const url = `LoadTicketData?action=showtimes&movieId=${movieId}&cinemaId=${cinemaId}&hallId=${hallId}&date=${date}`;
            const res = await fetch(url);
            if (res.ok) {
                const json = await res.json();
                if (json.showtimes?.length > 0) {
                    json.showtimes.forEach(st => {
                        const opt = document.createElement("option");
                        opt.value = st.id;
                        opt.textContent = st.showTime;
                        showtimeSelect.appendChild(opt);
                    });

                } else {
                    console.warn("No showtimes found:", json);
                }
            }
        } catch (e) {
            console.error("Error loading showtimes:", e);
        }
    }

    document.getElementById("show-date").addEventListener("change", function () {
        const cinemaId = cinemaSelect.value;
        const hallId = hallSelect.value;
        const date = this.value;
        loadShowtimes(cinemaId, hallId, date, movieId);
    });

    document.getElementById("show-time").addEventListener("change", async function () {
         console.log("Show-time changed, value:", this.value);
        const showtimeId = this.value;
        if (!showtimeId) return;

        try {
            const url = `LoadTicketData?action=availability&showtimeId=${showtimeId}`;
            const res = await fetch(url);
            if (res.ok) {
                const json = await res.json();
                document.getElementById("ticket-price").value = json.price;
                document.getElementById("available-seats").value = json.available;
                document.getElementById("ticket-qty").max = json.available;
                
                 console.log("Loaded price:", json.price, "Available seats:", json.available);
            }
        } catch (e) {
            console.error("Error fetching availability:", e);
        }
    });

}

async function addToCart() {
    const popup = new Notification();

    const showtimeId = document.getElementById("show-time").value;
    const qty = parseInt(document.getElementById("ticket-qty").value);

    if (!showtimeId || qty <= 0) {
        popup.error({ message: "Please select a showtime and valid quantity" });
        return;
    }

    try {
        const response = await fetch(`AddToCart?showtimeId=${showtimeId}&qty=${qty}`);
        if (response.ok) {
            const json = await response.json();
            if (json.status) {
                popup.success({ message: json.message });
            } else {
                popup.error({ message: json.message });
            }
        } else {
            popup.error({ message: "Failed to process your request" });
        }
    } catch (e) {
        popup.error({ message: "Error adding ticket to cart" });
        console.error("AddToCart error:", e);
    }
}

