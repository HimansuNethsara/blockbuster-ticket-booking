document.addEventListener("DOMContentLoaded", function () {
  const tabs = {
    accountDetailsTab: "accountDetailsForm",
    ordersTab: "ordersForm",
    addressesTab: "addressesForm"
  };

  Object.keys(tabs).forEach(function (tabId) {
    const tabButton = document.getElementById(tabId);
    const formId = tabs[tabId];

    if (tabButton) {
      tabButton.addEventListener("click", function () {
        // Remove active from all buttons
        document.querySelectorAll(".tab-button").forEach(function (btn) {
          btn.classList.remove("active");
        });

        // Hide all forms
        Object.values(tabs).forEach(function (form) {
          const formElement = document.getElementById(form);
          if (formElement) {
            formElement.style.display = "none";
          }
        });

        // Show selected form
        tabButton.classList.add("active");
        const activeForm = document.getElementById(formId);
        if (activeForm) {
          activeForm.style.display = "block";
        }
      });
    }
  });
});

function loadData(){
    getCityData();
    getUserData();
}

async function getUserData() {
  const response = await fetch("MyAccount");

  if (response.ok) {
    const json = await response.json();
    console.log(json);

    document.getElementById("username").innerHTML = `Hello ${json.firstName} ${json.lastName}`;
    document.getElementById("since").innerHTML = `BlockBuster Member Since ${json.since}`;

    document.getElementById("firstName").value = json.firstName;
    document.getElementById("lastName").value = json.lastName;
    document.getElementById("currentPassword").value = json.password;

    if (json.addressList && json.addressList.length > 0) {
      const address = json.addressList[0]; 
      const city = address.city?.name || "";
      const cityId = address.city?.id || "";

      document.getElementById("addName").value = `${json.firstName} ${json.lastName}`;
      document.getElementById("addEmail").value = address.user?.email || "";
      document.getElementById("contactNumber").value = address.contact || "";
      document.getElementById("addLineOne").value = address.lineOne || "";
      document.getElementById("addLineTwo").value = address.lineTwo || "";
      document.getElementById("addCode").value = address.postalCode || "";
      document.getElementById("addCity").value = city;

      document.getElementById("lineOne").value = address.lineOne || "";
      document.getElementById("lineTwo").value = address.lineTwo || "";
      document.getElementById("postalCode").value = address.postalCode || "";
      document.getElementById("citySelect").value = cityId;
      document.getElementById("contactInfo").value = address.contact || "";
      
    }
  }
}


async function getCityData(){
    const response = await fetch("CityData");
    
    if (response.ok) {
        const json = await response.json();
        const citySelect = document.getElementById("citySelect");
        json.forEach(city => {
            let option = document.createElement("option");
            option.innerHTML = city.name;
            option.value = city.id;
            citySelect.appendChild(option);
        });
    }
}

async function saveChanges() {
    hideMessage(); 

    const firstName = document.getElementById("firstName").value;
    const lastName = document.getElementById("lastName").value;
    const lineOne = document.getElementById("lineOne").value;
    const lineTwo = document.getElementById("lineTwo").value;
    const postalCode = document.getElementById("postalCode").value;
    const cityId = document.getElementById("citySelect").value;
    const contact = document.getElementById("contactInfo").value;
    const currentPassword = document.getElementById("currentPassword").value;
    const newPassword = document.getElementById("newPassword").value;
    const confirmPassword = document.getElementById("confirmPassword").value;

    const userDataObject = {
        firstName,
        lastName,
        lineOne,
        lineTwo,
        postalCode,
        cityId,
        contact,
        currentPassword,
        newPassword,
        confirmPassword
    };

    try {
        const response = await fetch("MyAccount", {
            method: "PUT",
            body: JSON.stringify(userDataObject),
            headers: {
                "Content-Type": "application/json"
            }
        });

        if (response.ok) {
            const json = await response.json();
            if (json.status) {
                showMessage("success", "Profile updated successfully.");
                loadData();
            } else {
                showMessage("error", json.message);
            }
        } else {
            showMessage("error", "Profile update failed. Please try again.");
        }
    } catch (error) {
        showMessage("error", "A network error occurred. Please try again.");
    }
}
