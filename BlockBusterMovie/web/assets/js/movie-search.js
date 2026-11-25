const notify = Notification();

let currentPage = 0;
const moviesPerPage = 8;

async function loadData() {
    try {
        const response = await fetch("LoadData");

        if (response.ok) {
            const json = await response.json();
            if (json.status) {
                loadSelect("genre", json.genreList, "name");
                searchMovie(0); 
                
                const movieCountElement = document.getElementById("movieCount");
                if (movieCountElement && json.allMovieCount !== undefined) {
                    movieCountElement.textContent = json.allMovieCount + " movies";
                }
                
            } else {
                notify.error({
                    title: "Data Load Failed",
                    message: json.message || "Unable to load data! Please try again later."
                });
            }
        } else {
            notify.error({
                title: "Server Error",
                message: "Unable to load data! Server returned an error."
            });
        }
    } catch (error) {
        notify.error({
            title: "Unexpected Error",
            message: error.message || "Something went wrong while loading data."
        });
    }
}


function loadSelect(selectId, list, property){
    const select = document.getElementById(selectId);
    select.innerHTML = ""; 

    list.forEach(item => {
        const option = document.createElement("option");
        option.value = item.id;
        option.innerHTML = item[property];
        select.appendChild(option);
    });
}

async function searchMovie(firstResult){
    
    currentPage = Math.floor(firstResult / moviesPerPage);
    
    const title = document.getElementById("title").value.trim();
    const genreSelect = document.getElementById("genre");
    const genreIds = Array.from(genreSelect.selectedOptions).map(opt => opt.value);

    const imdbFrom = parseFloat(document.getElementById("imdbFrom").value) || 0;
    const imdbTo = parseFloat(document.getElementById("imdbTo").value) || 10;

    const yearFrom = parseInt(document.getElementById("yearFrom").value) || 1900;
    const yearTo = parseInt(document.getElementById("yearTo").value) || new Date().getFullYear();
    
    const sortOption = document.getElementById("sortOption").value;

    const data = {
        firstResult,
        title,
        genres: genreIds,
        imdbFrom,
        imdbTo,
        yearFrom,
        yearTo,
        sortOption,
        maxResults: moviesPerPage
    };

    fetch("SearchMovie", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
    .then(resp => resp.json())
    .then(json => {
        updatePagination(json.totalCount || 0);

        const movieListContainer = document.querySelector(".flex-wrap-movielist");
        movieListContainer.innerHTML = ""; 

        if (json.movies && json.movies.length > 0) {
            json.movies.forEach(movie => {
                const div = document.createElement("div");
                div.className = "movie-item-style-2 movie-item-style-1";
                div.innerHTML = `
                    <img src="${movie.poster}" alt="">
                    <div class="hvr-inner">
                        <a href="moviesingle.html?id=${movie.id}"> Read more <i class="ion-android-arrow-dropright"></i> </a>
                    </div>
                    <div class="mv-item-infor">
                        <h6><a href="#">${movie.title}</a></h6>
                        <p class="rate"><i class="ion-android-star"></i> <span>${movie.imdbRating ? parseFloat(movie.imdbRating).toFixed(1) : 'N/A'}</span> /10
                        </p>
                    </div>
                `;
                movieListContainer.appendChild(div);
            });
        } else {
            movieListContainer.innerHTML = "<p>No movies found.</p>";
        }
    });
    
}
 
function updateMovieView(json) {
  const movieListContainer = document.querySelector(".flex-wrap-movielist");
  movieListContainer.innerHTML = "";

  if (json.movies && json.movies.length > 0) {
    json.movies.forEach(movie => {
      const div = document.createElement("div");
      div.className = "movie-item-style-2 movie-item-style-1";
      div.innerHTML = `
        <img src="${movie.poster || 'images/uploads/default.jpg'}" alt="">
        <div class="hvr-inner">
          <a href="moviesingle.html?id=${movie.id}"> Read more <i class="ion-android-arrow-dropright"></i> </a>
        </div>
        <div class="mv-item-infor">
          <h6><a href="#">${movie.title}</a></h6>
          <p class="rate"><i class="ion-android-star"></i> <span>${movie.imdbRating ? parseFloat(movie.imdbRating).toFixed(1) : 'N/A'}</span> /10
        </div>
      `;
      movieListContainer.appendChild(div);
    });
  } else {
    movieListContainer.innerHTML = "<p>No movies found.</p>";
  }

  updatePagination(json.totalCount || 0);
}

function updatePagination(totalCount) {
    const paginationContainer = document.getElementById("movie-pagination-container");
    paginationContainer.innerHTML = ""; // Clear old pagination

    const totalPages = Math.ceil(totalCount / moviesPerPage);

    // Previous button
    if (currentPage > 0) {
      const prevBtn = document.createElement("a");
      prevBtn.href = "#";
      prevBtn.textContent = "Prev";
      prevBtn.addEventListener("click", e => {
        e.preventDefault();
        searchMovie((currentPage - 1) * moviesPerPage);
      });
      paginationContainer.appendChild(prevBtn);
    }

    // Page numbers
    for (let i = 0; i < totalPages; i++) {
      const pageBtn = document.createElement("a");
      pageBtn.href = "#";
      pageBtn.textContent = (i + 1).toString();
      if (i === currentPage) {
        pageBtn.classList.add("active");
      }
      pageBtn.addEventListener("click", e => {
        e.preventDefault();
        searchMovie(i * moviesPerPage);
      });
      paginationContainer.appendChild(pageBtn);
    }

    // Next button
    if (currentPage < totalPages - 1) {
      const nextBtn = document.createElement("a");
      nextBtn.href = "#";
      nextBtn.textContent = "Next";
      nextBtn.addEventListener("click", e => {
        e.preventDefault();
        searchMovie((currentPage + 1) * moviesPerPage);
      });
      paginationContainer.appendChild(nextBtn);
    }
  }

    



