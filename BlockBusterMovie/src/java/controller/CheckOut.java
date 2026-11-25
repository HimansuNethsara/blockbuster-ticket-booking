package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Booking;
import hibernate.BookingItems;
import hibernate.BookingStatus;
import hibernate.Cart;
import hibernate.CartItem;
import hibernate.HibernateUtil;
import hibernate.Showtime;
import hibernate.Ticket;
import hibernate.User;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.PayHere;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "CheckOut", urlPatterns = {"/CheckOut"})
public class CheckOut extends HttpServlet {

    private static final int BOOKING_PENDING = 2; 

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        Session session = null;
        Transaction tx = null;

        try {
            User user = (User) request.getSession().getAttribute("user");

            if (user == null) {
                responseObject.addProperty("message", "Session expired. Please log in again.");
            } else {
                session = HibernateUtil.getSessionFactory().openSession();
                tx = session.beginTransaction();

                Criteria cartCriteria = session.createCriteria(Cart.class);
                cartCriteria.add(Restrictions.eq("user", user));
                cartCriteria.createAlias("cartStatus", "status");
                cartCriteria.add(Restrictions.eq("status.name", "Active"));
                Cart activeCart = (Cart) cartCriteria.uniqueResult();

                if (activeCart == null) {
                    responseObject.addProperty("message", "Your cart is empty.");
                } else {
                    Criteria itemCriteria = session.createCriteria(CartItem.class);
                    itemCriteria.add(Restrictions.eq("cart", activeCart));
                    List<CartItem> cartItems = itemCriteria.list();

                    if (cartItems.isEmpty()) {
                        responseObject.addProperty("message", "Your cart is empty.");
                    } else {
                        // Validate seat availability
                        boolean hasInvalidSeats = false;
                        for (CartItem item : cartItems) {
                            Showtime showtime = item.getShowtime();

                            Criteria ticketCriteria = session.createCriteria(Ticket.class);
                            ticketCriteria.add(Restrictions.eq("showtime", showtime));
                            Ticket ticket = (Ticket) ticketCriteria.uniqueResult();

                            int totalSeats = showtime.getHall().getTotalSeats();
                            int soldSeats = ticket != null ? ticket.getSoldCount() : 0;

                            if (soldSeats + item.getQuantity() > totalSeats) {
                                hasInvalidSeats = true;
                                break;
                            }
                        }

                        if (hasInvalidSeats) {
                            responseObject.addProperty("message", "Some tickets are sold out. Please update your cart.");
                        } else {
                            // Create booking
                            Booking booking = new Booking();
                            booking.setUser(user);
                            booking.setCreated_at(new Date());
                            booking.setBookingStatus((BookingStatus) session.get(BookingStatus.class, BOOKING_PENDING));

                            double totalAmount = 0;
                            StringBuilder itemNames = new StringBuilder();

                            session.save(booking);

                            for (CartItem item : cartItems) {
                                Showtime showtime = item.getShowtime();

                                Criteria ticketCriteria = session.createCriteria(Ticket.class);
                                ticketCriteria.add(Restrictions.eq("showtime", showtime));
                                Ticket ticket = (Ticket) ticketCriteria.uniqueResult();
                                if (ticket == null) {
                                    ticket = new Ticket();
                                    ticket.setShowtime(showtime);
                                    ticket.setSoldCount(0);
                                }
                                ticket.setSoldCount(ticket.getSoldCount() + item.getQuantity());
                                session.saveOrUpdate(ticket);

                                // Create booking item
                                BookingItems bookingItem = new BookingItems();
                                bookingItem.setBooking(booking);
                                bookingItem.setShowtime(showtime);
                                bookingItem.setQuantity(item.getQuantity());
                                bookingItem.setUnit_price(item.getUnit_price());
                                bookingItem.setSubtotal(item.getSubtotal());
                                session.save(bookingItem);

                                // Calculate total amount (including service fee)
                                double serviceFee = 100 * item.getQuantity();
                                totalAmount += item.getSubtotal() + serviceFee;

                                itemNames.append(showtime.getMovie().getTitle()).append(" x").append(item.getQuantity()).append(", ");

                                session.delete(item);
                            }

                            booking.setTotal_amount(totalAmount);
                            session.update(booking);

                            tx.commit();

                            // PayHere integration
                            String merchantId = "1231397"; 
                            String merchantSecret = "PAYHERE_MERCHANT_SECRET";
                            String orderId = "#BOOK" + booking.getId();
                            String currency = "LKR";
                            String formattedAmount = new DecimalFormat("0.00").format(totalAmount);

                            String merchantSecretMD5 = PayHere.generateMd5(merchantSecret);
                            String hash = PayHere.generateMd5(merchantId + orderId + formattedAmount + currency + merchantSecretMD5);

                            JsonObject payHereJson = new JsonObject();
                            
                            payHereJson.addProperty("sandbox", true);
                            payHereJson.addProperty("merchant_id", merchantId);
                            payHereJson.addProperty("return_url", "");
                            payHereJson.addProperty("cancel_url", "");
                            payHereJson.addProperty("notify_url", "https://d8b25ca82387.ngrok-free.app/BlockBusterMovie/VerifyPayments");

                            payHereJson.addProperty("order_id", orderId);
                            payHereJson.addProperty("items", itemNames.toString());
                            payHereJson.addProperty("amount", formattedAmount);
                            payHereJson.addProperty("currency", currency);
                            payHereJson.addProperty("hash", hash);

                            payHereJson.addProperty("first_name", user.getFirst_name());
                            payHereJson.addProperty("last_name", user.getLast_name());
                            payHereJson.addProperty("email", user.getEmail());
                            
                            payHereJson.addProperty("phone", "0766777788");
                            payHereJson.addProperty("address", "Online Payment");
                            payHereJson.addProperty("city", "Colombo");
                            payHereJson.addProperty("country", "Sri Lanka");

                            responseObject.addProperty("status", true);
                            responseObject.addProperty("message", "Checkout ready");
                            responseObject.add("payhereJson", gson.toJsonTree(payHereJson));
                        }
                    }
                }
            }

            if (tx != null && tx.isActive()) tx.commit();

        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            responseObject.addProperty("message", "Error: " + e.getMessage());
        } finally {
            if (session != null) session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }
}

