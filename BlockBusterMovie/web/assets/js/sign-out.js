async function signOut() {
    const popup = Notification();

    try {
        const response = await fetch("SignOut");

        if (response.ok) {
            const json = await response.json();
            if (json.status) {
                window.location = "sign-in.html";
            } else {
                popup.error({ message: "Logout unsuccessful. Reloading..." });
                window.location.reload();
            }
        } else {
            popup.error({ message: "Logout failed. Please try again." });
        }
    } catch (error) {
        popup.error({ message: "A network error occurred during logout." });
    }
}
