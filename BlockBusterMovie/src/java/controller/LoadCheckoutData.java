package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Cart;
import hibernate.CartItem;
import hibernate.CartItemDTO;
import hibernate.Hall;
import hibernate.HibernateUtil;
import hibernate.Showtime;
import hibernate.Ticket;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
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

@WebServlet(name = "LoadCheckoutData", urlPatterns = {"/LoadCheckoutData"})
public class LoadCheckoutData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        User user = (User) request.getSession().getAttribute("user");

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            Criteria cartCriteria = session.createCriteria(Cart.class);
            cartCriteria.add(Restrictions.eq("user", user));
            cartCriteria.createAlias("cartStatus", "status");
            cartCriteria.add(Restrictions.eq("status.name", "Active"));
            Cart activeCart = (Cart) cartCriteria.uniqueResult();

            if (activeCart == null) {
                responseObject.addProperty("message", "Your cart is empty");
            } else {
                Criteria itemCriteria = session.createCriteria(CartItem.class);
                itemCriteria.add(Restrictions.eq("cart", activeCart));
                List<CartItem> cartItems = itemCriteria.list();

                if (cartItems.isEmpty()) {
                    responseObject.addProperty("message", "Your cart is empty");
                } else {
                    List<CartItemDTO> dtoList = new ArrayList<>();
                    boolean hasInvalidSeats = false;

                    for (CartItem item : cartItems) {
                        Showtime showtime = item.getShowtime();
                        Hall hall = showtime.getHall();

                        Criteria ticketCriteria = session.createCriteria(Ticket.class);
                        ticketCriteria.add(Restrictions.eq("showtime", showtime));
                        Ticket ticket = (Ticket) ticketCriteria.uniqueResult();

                        int soldCount = (ticket != null) ? ticket.getSoldCount() : 0;
                        int availableSeats = hall.getTotalSeats() - soldCount;

                        if (item.getQuantity() > availableSeats) {
                            hasInvalidSeats = true;
                            break;
                        }

                        dtoList.add(new CartItemDTO(item));
                    }


                    if (hasInvalidSeats) {
                        responseObject.addProperty("message", "Some tickets are no longer available. Please update your cart.");
                    } else {
                        responseObject.addProperty("status", true);
                        responseObject.add("cartItems", gson.toJsonTree(dtoList));
                        responseObject.addProperty("message", "Checkout data loaded successfully");
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
