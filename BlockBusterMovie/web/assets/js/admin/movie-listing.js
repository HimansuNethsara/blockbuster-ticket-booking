const notify = Notification();

async function loadProductData(){
    
    try {
        const response = await fetch("LoadProductData");

        if (response.ok) {//success
            const json = await response.json();
            if (json.status) { //if true
                loadSelect("mpaaRating",json.mpaaRatingList,"value");
                loadSelect("genre",json.genreList,"name");
                loadSelect("actor",json.actorList,"name");
                loadSelect("director",json.directorList,"name");
                loadSelect("keyword",json.keywordList,"keyword");

                //multiselect
                new MultiSelect(document.querySelector('#genre'));
                new MultiSelect(document.querySelector('#actor'));
                new MultiSelect(document.querySelector('#director'));
                new MultiSelect(document.querySelector('#keyword'));

//                //filtering
//                setupActorFiltering(json.actorList); 
//                setupDirectorFiltering(json.directorList);
//                setupKeywordFiltering(json.keywordList);

            } else {
                notify.error({
                    title: "Data Load Failed",
                    message: json.message || "Unable to load product data! Please try again later."
                });
            }
        } else {
            notify.error({
                title: "Server Error",
                message: "Unable to load product data! Server returned an error."
            });
        }

    } catch (error) {
        notify.error({
            title: "Unexpected Error",
            message: error.message || "Something went wrong while loading product data."
        });
    }
}

function loadSelect(selectId, list, property){
    
    const select = document.getElementById(selectId);
    
    list.forEach(item => {
        const option = document.createElement("option");
        option.value = item.id;
        option.innerHTML = item[property];
        select.appendChild(option);
    });
    
}

async function saveProduct() {
    const title = document.getElementById("title").value;
    const description = document.getElementById("description").value;
    const imdb = document.getElementById("imdb").value;
    const rt_rating = document.getElementById("rt_rating").value;
    const mpaaRatingId = document.getElementById("mpaaRating").value;
    const trailer = document.getElementById("trailer").value;
    const releaseYear = document.getElementById("year").value;
    const runtime = document.getElementById("runtime").value;
    const image1 = document.getElementById("img1").files[0];

    const form = new FormData();
    form.append("title", title);
    form.append("description", description);
    form.append("imdb", imdb);
    form.append("rt_rating", rt_rating);
    form.append("mpaaRating", mpaaRatingId);
    form.append("trailer", trailer);
    form.append("year", releaseYear);
    form.append("runtime", runtime);
    form.append("image1", image1);

    document.querySelectorAll('input[name="genres[]"]').forEach(input => {
        form.append('genres', input.value);
    });

    document.querySelectorAll('input[name="actors[]"]').forEach(input => {
        form.append('actors', input.value);
    });

    document.querySelectorAll('input[name="directors[]"]').forEach(input => {
        form.append('directors', input.value);
    });

    document.querySelectorAll('input[name="keywords[]"]').forEach(input => {
        form.append('keywords', input.value);
    });
    
     for (let pair of form.entries()) {
        console.log(pair[0] + ': ' + pair[1]);
    }


    const response = await fetch("SaveMovie", {
        method: "POST",
        body: form
    });

    if (response.ok) {
        const json = await response.json();
        if (json.status) {
            notify.success({
                title: "Success",
                message: json.message || "Product saved successfully!"
            });

            document.getElementById("title").value = "";
            document.getElementById("description").value = "";
            document.getElementById("imdb").value = "";
            document.getElementById("rt_rating").value = "";
            document.getElementById("mpaaRating").value = "0";
            document.getElementById("trailer").value = "";
            document.getElementById("year").value = "";
            document.getElementById("runtime").value = "";
            document.getElementById("img1").value = "";
            
            document.querySelector('.multi-select#genre').multiSelectInstance.clearSelection();
            document.querySelector('.multi-select#actor').multiSelectInstance.clearSelection();
            document.querySelector('.multi-select#director').multiSelectInstance.clearSelection();
            document.querySelector('.multi-select#keyword').multiSelectInstance.clearSelection();


        } else {
            if (json.message === "Please Sign In!") {
                window.location = "sign-in.html";
            } else {
                notify.error({
                    title: "Error",
                    message: json.message || "An error occurred while saving the product."
                });
            }
        }
    } else {
        notify.error({
            title: "Server Error",
            message: "Failed to send request. Please try again later."
        });
    }
}

