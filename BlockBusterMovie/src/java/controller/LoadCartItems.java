package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Cart;
import hibernate.CartItem;
import hibernate.CartItemDTO;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
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

@WebServlet(name = "LoadCartItems", urlPatterns = {"/LoadCartItems"})
public class LoadCartItems extends HttpServlet {

    private static final int CART_EXPIRY_MINUTES = 45;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        Session session = null;
        Transaction tx = null;

        try {
            User user = (User) request.getSession().getAttribute("user");

            if (user != null) {
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
                        Date now = new Date();
                        Iterator<CartItem> iterator = cartItems.iterator();
                        boolean removedAny = false;

                        while (iterator.hasNext()) {
                            CartItem item = iterator.next();
                            long diffMinutes = (now.getTime() - item.getReservedAt().getTime()) / (1000 * 60);

                            if (diffMinutes > CART_EXPIRY_MINUTES) {
                                session.delete(item);
                                iterator.remove();
                                removedAny = true;
                            }
                        }

                        if (removedAny) {
                            tx.commit();
                            tx = session.beginTransaction();
                        }

                        if (cartItems.isEmpty()) {
                            responseObject.addProperty("message", "Your cart is empty (expired items removed)");
                        } else {
                            List<CartItemDTO> dtoList = new ArrayList<>();
                            for (CartItem item : cartItems) {
                                dtoList.add(new CartItemDTO(item));
                            }

                            responseObject.addProperty("status", true);
                            responseObject.add("cartItems", gson.toJsonTree(dtoList));
                            if (removedAny) {
                                responseObject.addProperty("message", "Some expired items were removed");
                            } else {
                                responseObject.addProperty("message", "Cart loaded successfully");
                            }

                        }
                    }
                }
            } else {
                 ArrayList<CartItem> sessionCart = (ArrayList<CartItem>) request.getSession().getAttribute("sessionCart");

                if (sessionCart != null && !sessionCart.isEmpty()) {
                    List<CartItemDTO> sessionDtoList = new ArrayList<>();
                    for (CartItem item : sessionCart) {
                        sessionDtoList.add(new CartItemDTO(item));
                    }

                    responseObject.addProperty("status", true);
                    responseObject.add("cartItems", gson.toJsonTree(sessionDtoList));
                    responseObject.addProperty("message", "Session cart loaded successfully");

                } else {
                    responseObject.addProperty("message", "Your cart is empty");
                }
            }
            
            if (tx != null && tx.isActive()) {
                tx.commit();
            }

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

