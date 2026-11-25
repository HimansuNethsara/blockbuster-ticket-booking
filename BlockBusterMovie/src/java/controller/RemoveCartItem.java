package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.CartItem;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

@WebServlet(name = "RemoveCartItem", urlPatterns = {"/RemoveCartItem"})
public class RemoveCartItem extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        String itemIdParam = request.getParameter("id");
        Session session = null;
        Transaction tx = null;

        try {
            if (itemIdParam == null || !itemIdParam.matches("\\d+")) {
                responseObject.addProperty("message", "Invalid item ID");
            } else {
                int itemId = Integer.parseInt(itemIdParam);
                User user = (User) request.getSession().getAttribute("user");

                if (user != null) {
                    
                    session = HibernateUtil.getSessionFactory().openSession();
                    tx = session.beginTransaction();

                    CartItem cartItem = (CartItem) session.get(CartItem.class, itemId);

                    if (cartItem == null) {
                        responseObject.addProperty("message", "Cart item not found");
                    } else {
                        session.delete(cartItem);
                        tx.commit();
                        responseObject.addProperty("status", true);
                        responseObject.addProperty("message", "Item removed from cart");
                    }
                } else {
                    
                    HttpSession httpSession = request.getSession();
                    ArrayList<CartItem> sessionCart = (ArrayList<CartItem>) httpSession.getAttribute("sessionCart");

                    if (sessionCart != null && !sessionCart.isEmpty()) {
                        boolean removed = sessionCart.removeIf(item -> item.getId() == itemId);

                        if (removed) {
                            httpSession.setAttribute("sessionCart", sessionCart);
                            responseObject.addProperty("status", true);
                            responseObject.addProperty("message", "Item removed from session cart");
                        } else {
                            responseObject.addProperty("message", "Item not found in session cart");
                        }
                    } else {
                        responseObject.addProperty("message", "Session cart is empty");
                    }
                }
            }
        } catch (NumberFormatException | HibernateException e) {
            if (tx != null) tx.rollback();
            responseObject.addProperty("message", "Error: " + e.getMessage());
        } finally {
            if (session != null) session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }
}

