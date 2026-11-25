package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.Showtime;
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

@WebServlet(name = "LoadAllShowTimes", urlPatterns = {"/LoadAllShowTimes"})
public class LoadAllShowTimes extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonArray showtimeArray = new JsonArray();

        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            Criteria criteria = session.createCriteria(Showtime.class, "st")
                .createAlias("st.movie", "m")
                .createAlias("st.cinema", "c")
                .createAlias("st.hall", "h")
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .addOrder(Order.asc("st.id"));

            List<Showtime> showtimes = criteria.list();

            for (Showtime st : showtimes) {
                JsonObject data = new JsonObject();
                data.addProperty("id", st.getId());
                data.addProperty("movie_name", st.getMovie().getTitle());
                data.addProperty("cinema_name", st.getCinema().getName());
                data.addProperty("hall_name", st.getHall().getName());
                data.addProperty("show_date", st.getShowDate().toString());
                data.addProperty("show_time", st.getShowTime().toString());
                data.addProperty("price", st.getPrice());

                showtimeArray.add(data);
            }
        } catch (HibernateException e) {
        } finally {
            session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(showtimeArray));
    }
    
}
