async function loadMovies() {
    const popup = new Notification();
    try {
        const response = await fetch('LoadAllMovies');
        if (!response.ok) {
            popup.error({ message: "Failed to fetch movie data." });
            return;
        }

        const movies = await response.json();

        const tbody = document.querySelector("table tbody");
        tbody.innerHTML = "";

        if (movies.length === 0) {
            popup.success({ message: "No movies found." });
            return;
        }

        movies.forEach(movie => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <th scope="row">${movie.id}</th>
                <td><img src="${movie.poster_url}" alt="Poster" style="height: 80px;"></td>
                <td>${movie.title}</td>
                <td>${movie.imdb_rating}</td>
                <td>${movie.rt_rating}</td>
                <td>${movie.release_year}</td>
                <td>${movie.runtime} min</td>
                <td>${movie.mpaa_rating}</td>
                <td>
                    <label class="switch">
                      <input type="checkbox" onchange="toggleMovieStatus(${movie.id}, this)" ${movie.statusId === 2 ? "checked" : ""}>
                      <span class="slider round"></span>
                    </label>
                </td>

            `;
            tbody.appendChild(row);
        });

        popup.success({ message: "Movie data loaded successfully." });

    } catch (error) {
        popup.error({ message: "Error: " + error.message });
    }
}

function printMovieList() {
    const title_top = "BlockBuster - Movie Management";
    const title = "Movie List";

    const table = document.querySelector("table").cloneNode(true);

    const baseUrl = window.location.origin + "/" + window.location.pathname.split("/")[1];
    table.querySelectorAll("img").forEach(img => {
        if (img.src.startsWith("/")) {
            img.src = baseUrl + img.getAttribute("src");
        } else if (!img.src.startsWith("http")) {
            img.src = baseUrl + "/" + img.getAttribute("src");
        }
    });

    table.querySelectorAll("tbody tr").forEach(row => {
        const statusCell = row.children[8]; 
        if (statusCell) {

            const checkbox = statusCell.querySelector("input[type=checkbox]");
            if (checkbox) {
                const statusText = checkbox.checked ? "Active" : "Pending";

                statusCell.textContent = statusText;
            }
        }
    });

    const tableHTML = table.outerHTML;

    const printWindow = window.open('', '', 'width=900,height=650');
    printWindow.document.write(`
        <html>
        <head>
            <title>${title_top}</title>
            <link rel="stylesheet" href="${baseUrl}/assets/css/vendor/bootstrap.min.css">
            <link rel="stylesheet" href="${baseUrl}/assets/fonts/font-awesome/css/font-awesome.min.css">
            <link rel="stylesheet" href="${baseUrl}/assets/css/admin/font.css">
            <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Muli:300,400,700">
            <link rel="stylesheet" href="${baseUrl}/assets/css/admin/style.default.css" id="theme-stylesheet">
            <style>
                body {
                    padding: 20px;
                }
                h2 {
                    text-align: center;
                    margin-bottom: 10px;
                }
                img {
                    height: 80px;
                }
            </style>
        </head>
        <body>
            <h2>${title}</h2>
            <div class="table-responsive">
                ${tableHTML}
            </div>
        </body>
        </html>
    `);

    printWindow.document.close();
    printWindow.focus();
    printWindow.print();
    printWindow.close();
}


async function toggleMovieStatus(movieId, checkbox) {
    const newStatusId = checkbox.checked ? 2 : 1;
    const popup = new Notification();

    try {
        const res = await fetch(`UpdateMovieStatus?id=${movieId}&status=${newStatusId}`, {
            method: "POST"
        });

        const json = await res.json();
        if (json.status) {
            popup.success({ message: json.message });
        } else {
            popup.error({ message: json.message });
            checkbox.checked = !checkbox.checked; // Revert toggle
        }
    } catch (err) {
        popup.error({ message: "Error: " + err.message });
        checkbox.checked = !checkbox.checked;
    }
}

