package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.BookingItems;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "LoadMyBookings", urlPatterns = {"/LoadMyBookings"})
public class LoadMyBookings extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonArray bookingArray = new JsonArray();

        User loggedUser = (User) request.getSession().getAttribute("user");
        if (loggedUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        try {

            Criteria criteria = session.createCriteria(BookingItems.class, "bi")
                .createAlias("bi.booking", "b")
                .createAlias("b.user", "u")
                .createAlias("bi.Showtime", "st")
                .createAlias("st.movie", "m")
                .createAlias("st.cinema", "c")
                .createAlias("st.hall", "h")
                .add(Restrictions.eq("u.id", loggedUser.getId())) 
                .addOrder(Order.desc("b.created_at"));

            List<BookingItems> bookingItems = criteria.list();

            for (BookingItems item : bookingItems) {
                JsonObject data = new JsonObject();

                data.addProperty("movie_poster", request.getContextPath() + "/movie-posters/" + item.getShowtime().getMovie().getId() + "/poster.png");
                data.addProperty("movie_name", item.getShowtime().getMovie().getTitle());

                data.addProperty("cinema_name", item.getShowtime().getCinema().getName());
                data.addProperty("hall_name", item.getShowtime().getHall().getName());
                data.addProperty("show_datetime", item.getShowtime().getShowDate() + " " + item.getShowtime().getShowTime());

                data.addProperty("price", item.getUnit_price());
                data.addProperty("tickets", item.getQuantity());
                data.addProperty("subtotal", item.getSubtotal());

                bookingArray.add(data);
            }

        } catch (HibernateException e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(bookingArray));
    }

}
