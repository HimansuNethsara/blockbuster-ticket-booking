async function signIn(event) {
    event.preventDefault();

    const popup = Notification();

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;

    const signInData = { email, password };
    const signInJson = JSON.stringify(signInData);

    try {
        const response = await fetch("SignIn", {
            method: "POST",
            body: signInJson,
            headers: { "Content-Type": "application/json" }
        });

        if (response.ok) {
            const json = await response.json();

            if (json.status) {
                popup.success({ 
                    message: "Sign-in successful!" 
                });

                if (json.message === "1") {
                    window.location = "verify-account.html";
                } else {
                    window.location = "index.html";
                }
            } else {
                popup.error({
                    message: json.message || "Invalid credentials."
                });
            }
        } else {
            popup.error({
                message: "Sign In failed. Please try again."
            });
        }
    } catch (error) {
        popup.error({
            message: "A network error occurred. Please try again."
        });
    }
}
