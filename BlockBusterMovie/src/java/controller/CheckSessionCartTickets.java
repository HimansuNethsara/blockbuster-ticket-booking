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
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "CheckSessionCartTickets", urlPatterns = {"/CheckSessionCartTickets"})
public class CheckSessionCartTickets extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        Session session = null;

        try {
            User user = (User) request.getSession().getAttribute("user");

            if (user != null) {
                // Get session cart
                ArrayList<CartItem> sessionCartItems = (ArrayList<CartItem>) request.getSession().getAttribute("sessionCart");
                
                if (sessionCartItems != null && !sessionCartItems.isEmpty()) {
                    session = HibernateUtil.getSessionFactory().openSession();
                    Transaction tr = session.beginTransaction();

                    Criteria cartCriteria = session.createCriteria(Cart.class);
                    cartCriteria.add(Restrictions.eq("user", user));
                    cartCriteria.createAlias("cartStatus", "status");
                    cartCriteria.add(Restrictions.eq("status.name", "Active"));
                    Cart activeCart = (Cart) cartCriteria.uniqueResult();

                    if (activeCart == null) {
 
                        activeCart = new Cart();
                        activeCart.setUser(user);

                        CartStatus activeStatus = (CartStatus) session.createCriteria(CartStatus.class)
                                .add(Restrictions.eq("name", "Active")).uniqueResult();

                        activeCart.setCartStatus(activeStatus);
                        activeCart.setCreated_at(new Date());
                        activeCart.setUpdated_at(new Date());
                        session.save(activeCart);
                    }

                    int addedCount = 0;
                    int skippedCount = 0;

                    for (CartItem sessionItem : sessionCartItems) {
                        Showtime showtime = (Showtime) session.get(Showtime.class, sessionItem.getShowtime().getId());

                        Date now = new Date();
                        java.sql.Date today = new java.sql.Date(now.getTime());
                        java.sql.Time currentTime = new java.sql.Time(now.getTime());

                        if (showtime == null) {
                            skippedCount++;
                            continue;
                        }

                        if (showtime.getShowDate().before(today)) {
                            skippedCount++;
                            continue;
                        }

                        if (showtime.getShowDate().equals(today) && showtime.getShowTime().before(currentTime)) {
                            skippedCount++;
                            continue;
                        }

                        int totalSeats = showtime.getHall().getTotalSeats();
                        int soldTickets = getSoldTickets(session, showtime.getId());
                        if (soldTickets + sessionItem.getQuantity() > totalSeats) {
                            skippedCount++;
                            continue;
                        }

                        Criteria itemCriteria = session.createCriteria(CartItem.class);
                        itemCriteria.add(Restrictions.eq("cart", activeCart));
                        itemCriteria.add(Restrictions.eq("showtime", showtime));
                        CartItem existingItem = (CartItem) itemCriteria.uniqueResult();

                        if (existingItem == null) {

                            CartItem newItem = new CartItem();
                            newItem.setCart(activeCart);
                            newItem.setShowtime(showtime);
                            newItem.setQuantity(sessionItem.getQuantity());
                            newItem.setUnit_price(sessionItem.getUnit_price());
                            newItem.setSubtotal((int) (sessionItem.getQuantity() * sessionItem.getUnit_price()));
                            newItem.setAdded_at(new Date());
                            newItem.setReservedAt(new Date()); 
                            session.save(newItem);
                        } else {
 
                            int newQty = existingItem.getQuantity() + sessionItem.getQuantity();
                            if (soldTickets + newQty <= totalSeats) {
                                existingItem.setQuantity(newQty);
                                existingItem.setSubtotal((int) (newQty * existingItem.getUnit_price()));
                                session.update(existingItem);
                            } else {
                                skippedCount++;
                                continue;
                            }
                        }
                        addedCount++;
                    }

                    tr.commit();

                    request.getSession().removeAttribute("sessionCart");

                    responseObject.addProperty("status", true);
                    responseObject.addProperty("message", "Session cart merged successfully. Added: " + addedCount + ", Skipped: " + skippedCount);

                } else {
                    responseObject.addProperty("message", "No session cart items to merge.");
                }

            } else {
                responseObject.addProperty("message", "User not logged in.");
            }

        } catch (HibernateException e) {
            e.printStackTrace();
            responseObject.addProperty("message", "Error: " + e.getMessage());
        } finally {
            if (session != null) session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(responseObject));
    }

    private int getSoldTickets(Session session, int showtimeId) {
        Criteria ticketCriteria = session.createCriteria(Ticket.class);
        ticketCriteria.add(Restrictions.eq("showtime.id", showtimeId));
        Ticket ticket = (Ticket) ticketCriteria.uniqueResult();
        return (ticket != null) ? ticket.getSoldCount() : 0;
    }
}

