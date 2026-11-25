async function adminSignOut() {
    const popup = Notification();

    try {
        const response = await fetch("SignOut");

        if (response.ok) {
            const json = await response.json();
            if (json.status) {
                window.location = "admin-login.html";
            } else {
                popup.error({ message: "Logout unsuccessful. Reloading..." });
                window.location.reload();
            }
        } else {
            popup.error({ message: "Admin logout failed. Please try again." });
        }
    } catch (error) {
        console.error("Error logging out admin:", error);
        popup.error({ message: "Network error during logout. Please try again." });
    }
}
