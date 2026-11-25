async function signUp() {
    const popup = Notification(); 

    const firstName = document.getElementById("firstName").value.trim();
    const lastName = document.getElementById("lastName").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;

    const user = {
        firstName,
        lastName,
        email,
        password
    };
    const userJson = JSON.stringify(user);

    try {
        const response = await fetch("SignUp", {
            method: "POST",
            body: userJson,
            headers: { "Content-Type": "application/json" }
        });

        if (response.ok) {
            const json = await response.json();
            if (json.status) {
                popup.success({ 
                    message: "Registration successful!" 
                });
                window.location = "verify-account.html";
            } else {
                popup.error({ message: json.message || "Registration failed." });
            }
        } else {
            popup.error({ message: "Registration failed. Please try again." });
        }
    } catch (error) {
        popup.error({ message: "A network error occurred. Please try again." });
    }
}
