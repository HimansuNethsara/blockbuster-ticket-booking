package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.Actors;
import hibernate.Genre;
import hibernate.HibernateUtil;
import hibernate.Movie;
import hibernate.Status;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "LoadHomeData", urlPatterns = {"/LoadHomeData"})
public class LoadHomeData extends HttpServlet {

    private static final int ACTIVE_STATUS_ID = 2; 
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();

        try {
            String type = request.getParameter("type");

            // Movie
            Criteria movieCriteria = session.createCriteria(Movie.class);
            Status activeStatus = (Status) session.get(Status.class, ACTIVE_STATUS_ID); 
            movieCriteria.add(Restrictions.eq("status", activeStatus));

            if ("recent".equalsIgnoreCase(type) || type == null) {
                movieCriteria.addOrder(Order.desc("id")); 
            } else if ("newyear".equalsIgnoreCase(type)) {
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                movieCriteria.add(Restrictions.eq("releaseYear", currentYear));
                movieCriteria.addOrder(Order.desc("id")); 
            } else if ("topimdbrated".equalsIgnoreCase(type)) {
                movieCriteria.add(Restrictions.ge("imdbRating", 8.0f));
                movieCriteria.addOrder(Order.desc("imdbRating"));      
            }
            movieCriteria.setFirstResult(0);
            movieCriteria.setMaxResults(12);

            List<Movie> movieList = movieCriteria.list();
            JsonArray moviesArray = new JsonArray();

            for (Movie m : movieList) {
                JsonObject movieObj = new JsonObject();
                movieObj.addProperty("id", m.getId());
                movieObj.addProperty("title", m.getTitle());
                movieObj.addProperty("imdb", m.getImdbRating());
                movieObj.addProperty("rating", m.getRtRating());
                movieObj.addProperty("image", "poster.png");

                moviesArray.add(movieObj);
            }

            responseObject.add("movies", moviesArray);

            Criteria actorCriteria = session.createCriteria(Actors.class);
            actorCriteria.addOrder(Order.desc("id"));
            actorCriteria.setFirstResult(0);
            actorCriteria.setMaxResults(4);

            List<Actors> actorList = actorCriteria.list();
            responseObject.add("actors", gson.toJsonTree(actorList));

            responseObject.addProperty("status", true);

        } catch (HibernateException e) {
            responseObject.addProperty("error", "Server error occurred.");
        } finally {
            session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }


}
