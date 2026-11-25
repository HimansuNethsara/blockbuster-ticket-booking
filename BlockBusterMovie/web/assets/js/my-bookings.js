async function loadMyBookings() {
    const popup = new Notification();
    try {
        const response = await fetch('LoadMyBookings');
        if (!response.ok) {
            popup.error({ message: "Failed to fetch booking data." });
            return;
        }

        const bookings = await response.json();

        const tbody = document.querySelector("#cart-item-container");
        tbody.innerHTML = "";

        if (bookings.length === 0) {
            popup.success({ message: "No bookings found." });
            return;
        }

        bookings.forEach(b => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td class="product-image"><img src="${b.movie_poster}" alt="Poster" style="height: 80px;"></td>
                <td class="product-title">${b.movie_name}</td>
                <td class="product-cinema">${b.cinema_name}</td>
                <td class="product-hall">${b.hall_name}</td>
                <td class="product-showtime">${b.show_datetime}</td>
                <td class="product-price">${b.price.toFixed(2)}</td>
                <td class="product-quantity">
                    <div class="pro-qty">
                        <span class="quantity-display">${b.tickets}</span>
                    </div>
                </td>
                <td class="product-subtotal">${b.subtotal.toFixed(2)}</td>
            `;
            tbody.appendChild(row);
        });

        popup.success({ message: "Bookings loaded successfully." });

    } catch (error) {
        popup.error({ message: "Error: " + error.message });
    }
}
