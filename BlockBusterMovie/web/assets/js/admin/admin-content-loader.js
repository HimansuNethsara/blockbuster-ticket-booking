function loadHeader() {
    const data = `
    <nav class="navbar navbar-expand-lg">
        <div class="search-panel">
            <div class="search-inner d-flex align-items-center justify-content-center">
                <div class="close-btn">Close <i class="fa fa-close"></i></div>
                <form id="searchForm" action="#">
                    <div class="form-group">
                        <input type="search" name="search" placeholder="What are you searching for...">
                        <button type="submit" class="submit">Search</button>
                    </div>
                </form>
            </div>
        </div>
        <div class="container-fluid d-flex align-items-center justify-content-between">
            <div class="navbar-header">
                <a href="admin-dashboard.html" class="navbar-brand">
                    <div class="brand-text brand-big visible text-uppercase">
                        <strong class="text-primary">Block</strong><strong>Buster</strong>
                    </div>
                    <div class="brand-text brand-sm">
                        <strong class="text-primary">B</strong><strong>B</strong>
                    </div>
                </a>
                <button class="sidebar-toggle"><i class="fa fa-long-arrow-left"></i></button>
            </div>
            <div class="right-menu list-inline no-margin-bottom">
                <div class="list-inline-item">
                    <a href="#" class="search-open nav-link"><i class="icon-magnifying-glass-browser"></i></a>
                </div>
                <div class="list-inline-item dropdown">
                    <a id="navbarDropdownMenuLink1" href="#" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" class="nav-link messages-toggle">
                        <i class="icon-email"></i><span class="badge dashbg-1">0</span>
                    </a>
                    <div aria-labelledby="navbarDropdownMenuLink1" class="dropdown-menu messages">
                        <a href="#" class="dropdown-item message d-flex align-items-center"><div class="content"></div></a>
                        <a href="#" class="dropdown-item text-center message"><strong>See All Messages <i class="fa fa-angle-right"></i></strong></a>
                    </div>
                </div>
                <div class="list-inline-item dropdown">
                    <a id="navbarDropdownMenuLink2" href="#" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" class="nav-link tasks-toggle">
                        <i class="icon-new-file"></i><span class="badge dashbg-3">0</span>
                    </a>
                    <div aria-labelledby="navbarDropdownMenuLink2" class="dropdown-menu tasks-list">
                        <a href="#" class="dropdown-item"></a>
                        <a href="#" class="dropdown-item text-center"><strong>See All Tasks <i class="fa fa-angle-right"></i></strong></a>
                    </div>
                </div>
                <div class="list-inline-item logout">
                    <a href="admin-login.html" onclick="adminSignOut();" class="nav-link">Logout <i class="icon-logout"></i></a>
                </div>
            </div>
        </div>
    </nav>`;
    
    document.querySelector("header").innerHTML = data;
}

function loadSidebar() {
    fetch('AdminAccount')
        .then(response => {
            if (!response.ok) {
                throw new Error("Not authorized");
            }
            return response.json();
        })
        .then(data => {
            const sidebarHTML = `
                <!-- Sidebar Header-->
                <div class="sidebar-header d-flex align-items-center">
                    <div class="avatar">
                        <img src="assets/images/avatar.png" alt="..." class="img-fluid rounded-circle">
                    </div>
                    <div class="title">
                        <h1 class="h5">${data.username}</h1>
                        <p>Admin</p>
                    </div>
                </div>
                <!-- Sidebar Navigation Menus-->
                <span class="heading">Main</span>
                <ul class="list-unstyled">
                    <li><a href="admin-dashboard.html"><i class="icon-home"></i>Home</a></li>
                    <li><a href="admin-user-table.html"><i class="icon-grid"></i>User Table</a></li>
                    <li><a href="admin-movie-table.html"><i class="icon-grid"></i>Movie Table</a></li>
                    <li><a href="admin-showtime-table.html"><i class="icon-grid"></i>Show Time Tables</a></li>
                    <li><a href="charts.html"><i class="fa fa-bar-chart"></i>Charts</a></li>
                    <li>
                        <a href="#exampledropdownDropdown" aria-expanded="false" data-toggle="collapse">
                            <i class="icon-windows"></i>Manage Movies & Theaters
                        </a>
                        <ul id="exampledropdownDropdown" class="collapse list-unstyled">
                            <li class="active"><a href="movies.html">Movie</a></li>
                            <li><a href="admin-cast-form.html">Add Actors and Directors</a></li>
                            <li><a href="admin-showtimes-cinemas.html">Manage Cinemas & Showtimes</a></li>
                        </ul>
                    </li>
                    <li><a href="admin-login.html" onclick="adminSignOut();"><i class="icon-logout"></i>Login page</a></li>
                </ul>
            `;

            document.querySelector("#sidebar").innerHTML = sidebarHTML;
        })
        .catch(error => {
            window.location.href = "admin-login.html";
        });
}

function loadFooter() {
    const data = `
        <div class="footer__block block no-margin-bottom">
            <div class="container-fluid text-center">
                <p class="no-margin-bottom">2025 &copy; BlockBuster.</p>
            </div>
        </div>
    `;
    document.querySelector("footer.footer").innerHTML = data;
}


loadHeader();
loadSidebar();
loadFooter();

