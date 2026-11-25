async function sendPassword() {
    const popup = Notification(); 

    const emailInput = document.getElementById("email");
    const email = emailInput.value.trim();

    if (!email) {
        popup.error({ message: "Please enter your email." });
        return;
    }

    try {
        const response = await fetch("ForgotPassword", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ email: email })
        });

        if (response.ok) {
            const json = await response.json();

            if (json.status) {
                popup.success({ message: json.message || "Password has been sent to your email." });
            } else {
                popup.error({ message: json.message || "This email is not registered." });
            }
        } else {
            popup.error({ message: "Server error. Please try again later." });
        }
    } catch (error) {
        console.error("Forgot Password Error:", error);
        popup.error({ message: "Network error. Please check your connection." });
    }
}
