async function adminSignIn(event) {
    event.preventDefault();
    const popup = Notification(); 

    const username = document.getElementById("login-username").value;
    const password = document.getElementById("login-password").value;

    const data = { username, password };

    try {
        const response = await fetch("AdminSignIn", {
            method: "POST",
            body: JSON.stringify(data),
            headers: { "Content-Type": "application/json" }
        });

        const result = await response.json();

        if (result.status) {
            popup.success({ message: "Welcome back!" });
            window.location = "admin-dashboard.html";
        } else {
            popup.error({ message: result.message || "Invalid admin credentials." });
        }

    } catch (error) {
        popup.error({ message: "A network error occurred." });
    }
}
