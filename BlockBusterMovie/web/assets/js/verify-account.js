const inputs = document.querySelectorAll(".code-inputs input");
const verifyBtn = document.getElementById("verifyBtn");

inputs.forEach((input, index) => {
  input.addEventListener("input", (e) => {
    if (!/^\d$/.test(e.target.value)) {
      e.target.value = "";
      return;
    }

    input.classList.toggle("filled", e.target.value !== "");

    if (e.target.value.length === 1 && index < inputs.length - 1) {
      inputs[index + 1].focus();
    }

    checkAllFieldsFilled();
  });

  input.addEventListener("keydown", (e) => {
    if (e.key === "Backspace" && !e.target.value && index > 0) {
      inputs[index - 1].focus();
    }
  });
});

function checkAllFieldsFilled() {
  const allFilled = Array.from(inputs).every((input) => input.value);
  verifyBtn.disabled = !allFilled;
}

async function verifyAccount() {
  const popup = Notification();

  const code = Array.from(inputs).map(input => input.value).join("");
  const verification = { verificationCode: code };
  const verificationJson = JSON.stringify(verification);

  try {
    const response = await fetch("VerifyAccount", {
      method: "POST",
      body: verificationJson,
      headers: { "Content-Type": "application/json" }
    });

    const json = await response.json();

    if (response.ok) {
      if (json.status) {
        popup.success({ message: "Account verified!" });
        window.location = "index.html";
      } else {
        if (json.message === "1") {
          window.location = "sign-in.html";
        } else {
          popup.error({ message: json.message || "Verification failed." });
        }
      }
    } else {
      popup.error({ message: "Verification failed. Please try again." });
    }
  } catch (error) {
    popup.error({ message: "A network error occurred. Please try again." });
  }
}
