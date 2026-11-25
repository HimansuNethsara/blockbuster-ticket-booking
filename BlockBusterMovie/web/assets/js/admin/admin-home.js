function adminStatistics(){
    
    loadUserStats();
    
}

async function loadUserStats() {
    try {
        const response = await fetch('AdminDashboardStat');
        const data = await response.json();

        const totalUsers = data.totalUserCount;
        const newUsers = data.newUserCount;
        
        const totalBookings = data.totalTicketCount;
        const newBookings = data.newTicketCount;
        
        const totalRevenue = data.totalRevenue;
        const dailyRevenue = data.dailyRevenue;
        
        const thisYearReleases = data.thisYearReleases;
        const totalMovies = data.totalMovies;

        document.getElementById("total-user-count").textContent = totalUsers;
        document.getElementById("new-user-count").textContent = newUsers;
        
        document.getElementById("total-booking-count").textContent = totalBookings;
        document.getElementById("new-booking-count").textContent = newBookings;
        
        document.getElementById("total-revenue-count").textContent = `Rs.${totalRevenue.toFixed(2)}`;
        document.getElementById("daily-revenue-count").textContent = `Rs.${dailyRevenue.toFixed(2)}`;
        
        document.getElementById("this-year-releases").textContent = thisYearReleases;
        document.getElementById("total-movie-count").textContent = totalMovies;

        // Calculate progress values
        const totalUserGoal = 1000; // 
        const totalUserProgress = Math.min((totalUsers / totalUserGoal) * 100, 100);
        const newUserProgress = totalUsers > 0 ? (newUsers / totalUsers) * 100 : 0;

        const totalUserBar = document.getElementById("total-user-bar");
        totalUserBar.style.width = totalUserProgress + "%";
        totalUserBar.setAttribute("aria-valuenow", totalUserProgress.toFixed(0));

        const newUserBar = document.getElementById("new-user-bar");
        newUserBar.style.width = newUserProgress + "%";
        newUserBar.setAttribute("aria-valuenow", newUserProgress.toFixed(0));
        
        const totalBookingGoal = 1000; 
        const totalBookingProgress = Math.min((totalBookings / totalBookingGoal) * 100, 100);
        const newBookingProgress = totalBookings > 0 ? (newBookings / totalBookings) * 100 : 0;
        
        const totalBookingBar = document.getElementById("total-booking-bar");
        totalBookingBar.style.width = totalBookingProgress + "%";
        totalBookingBar.setAttribute("aria-valuenow", totalBookingProgress.toFixed(0));

        const newBookingBar = document.getElementById("new-booking-bar");
        newBookingBar.style.width = newBookingProgress + "%";
        newBookingBar.setAttribute("aria-valuenow", newBookingProgress.toFixed(0));
        
        const totalRevenueGoal = 1000000;
        const totalRevenueProgress = Math.min((totalRevenue / totalRevenueGoal) * 100, 100);
        document.getElementById("total-revenue-bar").style.width = totalRevenueProgress + "%";
        document.getElementById("total-revenue-bar").setAttribute("aria-valuenow", totalRevenueProgress.toFixed(0));
        
        const dailyRevenueGoal = 100000;
        const dailyRevenueProgress = Math.min((dailyRevenue / dailyRevenueGoal) * 100, 100);
        document.getElementById("daily-revenue-bar").style.width = dailyRevenueProgress + "%";
        document.getElementById("daily-revenue-bar").setAttribute("aria-valuenow", dailyRevenueProgress.toFixed(0));
        
        const thisYearGoal = 50; 
        const thisYearProgress = Math.min((thisYearReleases / thisYearGoal) * 100, 100);
        document.getElementById("this-year-bar").style.width = thisYearProgress + "%";
        document.getElementById("this-year-bar").setAttribute("aria-valuenow", thisYearProgress.toFixed(0));

        const totalMovieGoal = 1000;
        const totalMovieProgress = Math.min((totalMovies / totalMovieGoal) * 100, 100);
        document.getElementById("total-movie-bar").style.width = totalMovieProgress + "%";
        document.getElementById("total-movie-bar").setAttribute("aria-valuenow", totalMovieProgress.toFixed(0));
        
        // Top 3 users update
        const topUsers = data.topUsers;
        for (let i = 0; i < topUsers.length; i++) {
            const user = topUsers[i];
            document.getElementById(`user${i + 1}-name`).textContent =
                user.first_name + " " + user.last_name;
            document.getElementById(`user${i + 1}-email`).textContent =
                "@" + user.email.split("@")[0];
            document.getElementById(`user${i + 1}-bookings`).textContent =
                user.bookingCount + " Tickets";
        }

    } catch (error) {
        console.error("Failed to load stats", error);
    }
}