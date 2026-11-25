async function loadUsers() {
    const popup = new Notification();
    try {
        const response = await fetch('LoadAllUsers');
        if (!response.ok) {
            popup.error({ message: "Failed to fetch user data." });
            return;
        }

        const users = await response.json();

        const tbody = document.querySelector("table tbody");
        tbody.innerHTML = ""; 

        if (users.length === 0) {
            popup.success({ message: "No users found." });
            return;
        }

        users.forEach(user => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <th scope="row">${user.id}</th>
                <td>${user.first_name}</td>
                <td>${user.last_name}</td>
                <td>${user.email}</td>
                <td>${user.status}</td>
                <td>${user.joined}</td>
            `;
            tbody.appendChild(row);
        });

        popup.success({ message: "User data loaded successfully." });

    } catch (error) {
        popup.error({ message: "Error: " + error.message });
    }
}

function printUserList() {
    const title_top = "BlockBuster - User Management";
    const title = "User List";

    const tableHTML = document.querySelector("table").outerHTML;

    const printWindow = window.open('', '', 'width=900,height=650');

    printWindow.document.write(`
        <html>
        <head>
            <title>${title_top}</title>

            <!-- Load external CSS -->
               <link rel="stylesheet" href="assets/css/vendor/bootstrap.min.css">
                <!-- Font Awesome CSS-->
                <link rel="stylesheet" href="assets/fonts/font-awesome/css/font-awesome.min.css">
                <!-- Custom Font Icons CSS-->
                <link rel="stylesheet" href="assets/css/admin/font.css">
                <!-- Google fonts - Muli-->
                <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Muli:300,400,700">
                <!-- theme stylesheet-->
                <link rel="stylesheet" href="assets/css/admin/style.default.css" id="theme-stylesheet">

            <!-- Add optional inline styles -->
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


