const notify = Notification();

async function addActor() {
    const name = document.getElementById("actor-name").value.trim();
    const image = document.getElementById("img1").files[0];

    if (!name || !image) {
        notify.error({
            title: "Missing Fields",
            message: "Please fill in both name and image."
        });
        return;
    }

    const formData = new FormData();
    formData.append("name", name);
    formData.append("image", image);

    try {
        const response = await fetch("SaveActor", {
            method: "POST",
            body: formData
        });

        if (response.ok) {
            const json = await response.json();

            if (json.status) {
                notify.success({
                    title: "Success",
                    message: json.message || "Actor added successfully!"
                });

                document.getElementById("actor-name").value = "";
                document.getElementById("img1").value = "";

            } else {
                notify.error({
                    title: "Add Failed",
                    message: json.message || "Failed to add actor."
                });
            }

        } else {
            notify.error({
                title: "Server Error",
                message: "Failed to connect to the server."
            });
        }

    } catch (error) {
        notify.error({
            title: "Unexpected Error",
            message: error.message || "Something went wrong."
        });
    }
}

async function addDirector() {
    const name = document.getElementById("director-name").value.trim();
    const image = document.getElementById("img2").files[0];

    if (!name || !image) {
        notify.error({
            title: "Missing Fields",
            message: "Please fill in both name and image."
        });
        return;
    }

    const formData = new FormData();
    formData.append("name", name);
    formData.append("image", image);

    try {
        const response = await fetch("SaveDirector", {
            method: "POST",
            body: formData
        });

        const json = await response.json();
        if (json.status) {
            notify.success({
                title: "Success",
                message: json.message || "Director added successfully!"
            });
            document.getElementById("director-name").value = "";
            document.getElementById("img2").value = "";
        } else {
            notify.error({
                title: "Failed",
                message: json.message || "Failed to add director."
            });
        }

    } catch (err) {
        notify.error({
            title: "Error",
            message: err.message || "Something went wrong."
        });
    }
}
