async function loadCartItems() {
    const popup = new Notification();
    try {
        const response = await fetch("LoadCartItems");
        if (!response.ok) {
            popup.error({ message: "OOPS..Something Went Wrong!" });
            return;
        }

        const json = await response.json();
        console.log("Cart Response:", json); 

        if (!json.status || !json.cartItems || !Array.isArray(json.cartItems)) {
            popup.error({ message: json.message || "Cart loading failed" });
 
            document.getElementById("cart-item-container").innerHTML = `
                <tr>
                    <td colspan="9" style="text-align:center;">Your cart is empty</td>
                </tr>`;
            document.getElementById("order-total-quantity").innerText = 0;
            document.getElementById("order-total-amount").innerText = `Rs. 0.00`;
            return;
        }

        const cartItemContainer = document.getElementById("cart-item-container");
        cartItemContainer.innerHTML = "";

        let total = 0;
        let totalQty = 0;

        json.cartItems.forEach(item => {
            const subtotal = item.subtotal || 0;
            total += subtotal;
            totalQty += item.quantity || 0;

            const row = `
                <tr class="cart-row">
                    <td class="product-remove">
                        <a href="#" onclick="removeCartItem(${item.id})">
                            <i class="fas fa-times"></i>
                        </a>
                    </td>
                    <td class="product-thumbnail">
                        <img src="${item.posterPath}" alt="${item.movieTitle}">
                    </td>
                    <td class="product-title">${item.movieTitle}</td>
                    <td class="product-cinema">${item.cinemaName}</td>
                    <td class="product-hall">${item.hallName}</td>
                    <td class="product-showtime">${item.showDate} | ${item.showTime}</td>
                    <td class="product-price">Rs. ${item.unitPrice.toFixed(2)}</td>
                    <td class="product-quantity">
                        <div class="pro-qty">
                            <span class="quantity-display" data-id="${item.id}">${item.quantity}</span>
                        </div>
                    </td>
                    <td class="product-subtotal">Rs. ${item.subtotal.toFixed(2)}</td>
                </tr>`;

            cartItemContainer.innerHTML += row;
        });

        // Update totals
        document.getElementById("order-total-quantity").innerText = totalQty;
        document.getElementById("order-total-amount").innerText = `Rs. ${total.toFixed(2)}`;

    } catch (error) {
        console.error("Error loading cart:", error);
        popup.error({ message: "OOPS..Something Went Wrong!" });
    }
}


async function removeCartItem(itemId) {
    const popup = new Notification();
    try {
        const res = await fetch(`RemoveCartItem?id=${itemId}`);
        if (res.ok) {
            const json = await res.json();
            if (json.status) {
                popup.success({ message: json.message });

                await loadCartItems(); 

            } else {
                popup.error({ message: json.message });
            }
        } else {
            popup.error({ message: "Failed to remove item" });
        }
    } catch (error) {
        popup.error({ message: "Error: " + error.message });
    }
}

