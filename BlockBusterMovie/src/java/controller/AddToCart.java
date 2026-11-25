package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Cart;
import hibernate.CartItem;
import hibernate.CartStatus;
import hibernate.HibernateUtil;
import hibernate.Showtime;
import hibernate.Ticket;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "AddToCart", urlPatterns = {"/AddToCart"})
public class AddToCart extends HttpServlet {
    
    private static final int CART_EXPIRY_MINUTES = 45;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonObject json = new JsonObject();
        json.addProperty("status", false);

        HttpSession httpSession = request.getSession();
        User user = (User) httpSession.getAttribute("user");
        
        String showtimeIdStr = request.getParameter("showtimeId");
        String qtyStr = request.getParameter("qty");

        Session session = null;
        Transaction tx = null;

        try {
            if (showtimeIdStr == null || qtyStr == null) {
                json.addProperty("message", "Showtime and quantity are required");
            } else if (!showtimeIdStr.matches("\\d+") || !qtyStr.matches("\\d+")) {
                json.addProperty("message", "Invalid input format");
            } else {
                int showtimeId = Integer.parseInt(showtimeIdStr);
                int qty = Integer.parseInt(qtyStr);

                if (qty <= 0) {
                    json.addProperty("message", "Quantity must be greater than zero");
                } else {
                    session = HibernateUtil.getSessionFactory().openSession();
                    tx = session.beginTransaction();

                    Showtime showtime = (Showtime) session.get(Showtime.class, showtimeId);
                    if (showtime == null) {
                        json.addProperty("message", "Showtime not found");
                    } else {

                        LocalDate today = LocalDate.now();
                        LocalTime nowTime = LocalTime.now();
                        LocalDate showDate = showtime.getShowDate().toLocalDate();
                        LocalTime showTime = showtime.getShowTime().toLocalTime();

                        if (showDate.isBefore(today) || (showDate.isEqual(today) && showTime.isBefore(nowTime))) {
                            json.addProperty("message", "Cannot book tickets for past showtime");
                        } else {

                            int totalSeats = showtime.getHall().getTotalSeats();

                            Criteria ticketCriteria = session.createCriteria(Ticket.class);
                            ticketCriteria.add(Restrictions.eq("showtime", showtime));
                            Ticket ticket = (Ticket) ticketCriteria.uniqueResult();

                            int soldCount = (ticket != null) ? ticket.getSoldCount() : 0;
                            int available = totalSeats - soldCount;

                            if (qty > available) {
                                json.addProperty("message", "Not enough seats available");
                            } else {

                                if (user == null) {
                                    // Handle session cart for not logged-in user
                                    ArrayList<CartItem> sessionCart = (ArrayList<CartItem>) httpSession.getAttribute("sessionCart");
                                    if (sessionCart == null) {
                                        sessionCart = new ArrayList<>();
                                    }

                                    boolean found = false;
                                    for (CartItem ci : sessionCart) {
                                        if (ci.getShowtime().getId() == showtimeId) {
                                            int newQty = ci.getQuantity() + qty;
                                            if (newQty > available) {
                                                json.addProperty("message", "Not enough seats available to update quantity");
                                                response.setContentType("application/json");
                                                response.getWriter().write(gson.toJson(json));
                                                return;
                                            }
                                            ci.setQuantity(newQty);
                                            ci.setSubtotal((int) (ci.getUnit_price() * newQty));
                                            found = true;
                                            break;
                                        }
                                    }

                                    if (!found) {
                                        CartItem newItem = new CartItem();
                                        newItem.setShowtime(showtime);
                                        newItem.setQuantity(qty);
                                        newItem.setUnit_price(showtime.getPrice());
                                        newItem.setSubtotal((int) (showtime.getPrice() * qty));
                                        newItem.setAdded_at(new Date());
                                        sessionCart.add(newItem);
                                    }

                                    httpSession.setAttribute("sessionCart", sessionCart);

                                    json.addProperty("message", "Tickets added to session cart (not logged in)");
                                    json.addProperty("status", true);

                                    response.setContentType("application/json");
                                    response.getWriter().write(gson.toJson(json));
                                    return;

                                } else {

                                    Criteria cartCriteria = session.createCriteria(Cart.class);
                                    cartCriteria.add(Restrictions.eq("user", user));
                                    cartCriteria.createAlias("cartStatus", "status");
                                    cartCriteria.add(Restrictions.eq("status.name", "Active"));

                                    Cart activeCart = (Cart) cartCriteria.uniqueResult();
                                    if (activeCart == null) {
                                        CartStatus activeStatus = (CartStatus) session.createCriteria(CartStatus.class)
                                                .add(Restrictions.eq("name", "Active"))
                                                .uniqueResult();
                                        activeCart = new Cart();
                                        activeCart.setUser(user);
                                        activeCart.setCartStatus(activeStatus);
                                        activeCart.setCreated_at(new Date());
                                        activeCart.setUpdated_at(new Date());
                                        session.save(activeCart);
                                    } else {
                                        activeCart.setUpdated_at(new Date());
                                        session.update(activeCart);
                                    }

                                    Criteria itemCriteria = session.createCriteria(CartItem.class);
                                    itemCriteria.add(Restrictions.eq("cart", activeCart));
                                    itemCriteria.add(Restrictions.eq("showtime", showtime));

                                    CartItem item = (CartItem) itemCriteria.uniqueResult();
                                    double unitPrice = showtime.getPrice();
                                    
                                    if (item != null && item.getReservedAt() != null) {
                                        long diffMinutes = (new Date().getTime() - item.getReservedAt().getTime()) / (1000 * 60);
                                        if (diffMinutes > CART_EXPIRY_MINUTES) {
                                            session.delete(item);
                                            item = null; // treat as new item
                                        }
                                    }

                                    if (item == null) {
                                        item = new CartItem();
                                        item.setCart(activeCart);
                                        item.setShowtime(showtime);
                                        item.setQuantity(qty);
                                        item.setUnit_price(unitPrice);
                                        item.setSubtotal((int) (unitPrice * qty));
                                        item.setAdded_at(new Date());
                                        item.setReservedAt(new Date());
                                        session.save(item);
                                        json.addProperty("message", "Ticket added to cart");
                                    } else {
                                        int newQty = item.getQuantity() + qty;

                                        if (newQty > available) {
                                            json.addProperty("message", "Not enough seats to update quantity");
                                        } else {
                                            item.setQuantity(newQty);
                                            item.setSubtotal((int) (unitPrice * newQty));
                                            item.setReservedAt(new Date());
                                            session.update(item);
                                            json.addProperty("message", "Cart updated");
                                        }
                                    }

                                    json.addProperty("status", true);
                                    tx.commit();
                                }
                            }
                        }
                    }
                }

            }
        } catch (NumberFormatException | HibernateException e) {
            if (tx != null) tx.rollback();
            json.addProperty("message", "Server error: " + e.getMessage());
        } finally {
            if (session != null) session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(json));
    }
}


