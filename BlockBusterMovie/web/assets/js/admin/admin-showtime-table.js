async function loadShowtimes() {
    const popup = new Notification();
    try {
        const response = await fetch('LoadAllShowTimes');
        if (!response.ok) {
            popup.error({ message: "Failed to fetch showtime data." });
            return;
        }

        const showtimes = await response.json();

        const tbody = document.querySelector("table tbody");
        tbody.innerHTML = "";

        if (showtimes.length === 0) {
            popup.success({ message: "No showtimes found." });
            return;
        }

        showtimes.forEach(st => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <th scope="row">${st.id}</th>
                <td>${st.movie_name}</td>
                <td>${st.cinema_name}</td>
                <td>${st.hall_name}</td>
                <td>${st.show_date}</td>
                <td>${st.show_time}</td>
                <td>${st.price.toFixed(2)}</td>
            `;
            tbody.appendChild(row);
        });

        popup.success({ message: "Showtime data loaded successfully." });

    } catch (error) {
        popup.error({ message: "Error: " + error.message });
    }
}

function printShowtimeList() {
    const title_top = "BlockBuster - Showtime Management";
    const title = "Showtime List";

    const table = document.querySelector("table").cloneNode(true);

    const tableHTML = table.outerHTML;

    const printWindow = window.open('', '', 'width=900,height=650');
    printWindow.document.write(`
        <html>
        <head>
            <title>${title_top}</title>
            <link rel="stylesheet" href="assets/css/vendor/bootstrap.min.css">
            <link rel="stylesheet" href="assets/fonts/font-awesome/css/font-awesome.min.css">
            <link rel="stylesheet" href="assets/css/admin/font.css">
            <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Muli:300,400,700">
            <link rel="stylesheet" href="assets/css/admin/style.default.css" id="theme-stylesheet">
            <style>
                body {
                    padding: 20px;
                }
                h2 {
                    text-align: center;
                    margin-bottom: 10px;
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



