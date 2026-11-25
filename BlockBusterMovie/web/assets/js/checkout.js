payhere.onCompleted = function onCompleted(orderId) {
    console.log("Payment completed. OrderID:" + orderId);
    const popup = new Notification();
    popup.success({ message: "Payment completed! Order ID: " + orderId });
};

payhere.onDismissed = function onDismissed() {
    console.log("Payment dismissed");
};

payhere.onError = function onError(error) {
    console.log("Error: " + error);
};

async function loadCheckoutData() {
    const popup = new Notification();

    try {
        const response = await fetch("LoadCheckoutData");

        if (response.status === 401) {
            window.location = "sign-in.html"; 
            return;
        }

        if (!response.ok) {
            popup.error({ message: "Failed to load checkout data" });
            return;
        }

        const json = await response.json();
        console.log("Checkout Data:", json);

        if (!json.status || !json.cartItems || json.cartItems.length === 0) {
            popup.error({ message: json.message || "Cart is empty" });
            window.location = "index.html";
            return;
        }

        const bookingBody = document.getElementById("booking-summary-body");
        bookingBody.innerHTML = "";

        let subtotal = 0;
        let totalTickets = 0;

        json.cartItems.forEach(item => {
            const row = `
                <tr class="booking-item-row">
                    <td>
                        <span class="movie-title">${item.movieTitle}</span><br>
                        <small>
                            Cinema: <span class="cinema-name">${item.cinemaName}</span>, 
                            Hall: <span class="hall-name">${item.hallName}</span><br>
                            Date: <span class="show-date">${item.showDate}</span>, 
                            Time: <span class="show-time">${item.showTime}</span><br>
                            Tickets: <span class="ticket-qty">${item.quantity}</span>
                        </small>
                    </td>
                    <td>Rs. <span class="booking-subtotal">${item.subtotal.toFixed(2)}</span></td>
                </tr>`;
            bookingBody.innerHTML += row;

            subtotal += item.subtotal;
            totalTickets += item.quantity;
        });

        const serviceFee = totalTickets * 100; // Rs. 100 per ticket
        const totalAmount = subtotal + serviceFee;

        bookingBody.innerHTML += `
            <tr class="order-subtotal">
                <td>Subtotal</td>
                <td>Rs. <span id="booking-subtotal-amount">${subtotal.toFixed(2)}</span></td>
            </tr>
            <tr class="order-service-charge">
                <td>Service Fee</td>
                <td>Rs. <span id="booking-service-fee">${serviceFee.toFixed(2)}</span></td>
            </tr>
            <tr class="order-total">
                <td>Total</td>
                <td class="order-total-amount">Rs. <span id="booking-total-amount">${totalAmount.toFixed(2)}</span></td>
            </tr>`;
    } catch (error) {
        console.error("Error loading checkout:", error);
        popup.error({ message: "OOPS..Something went wrong!" });
    }
}

async function checkout() {
    console.log("Starting checkout...");
    
    const response = await fetch("CheckOut", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({})
    });

    if (!response.ok) {
        console.error("Checkout servlet error:", response.status, await response.text());
        return;
    }

    const json = await response.json();
    console.log("Checkout Response:", json);

    if (json.status) {
        console.log("PayHere Data:", json.payhereJson);

        // Extra validation before starting payment
        if (!json.payhereJson.merchant_id || !json.payhereJson.hash) {
            console.error("Missing PayHere mandatory fields:", json.payhereJson);
            return;
        }

        try {
            payhere.startPayment(json.payhereJson);
        } catch (err) {
            console.error("PayHere startPayment error:", err);
        }
    } else {
        console.error("Checkout error:", json.message);
    }
}


