package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Genre;
import hibernate.HibernateUtil;
import hibernate.Movie;
import hibernate.Status;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
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

@WebServlet(name = "LoadData", urlPatterns = {"/LoadData"})
public class LoadData extends HttpServlet {
    
    private static final int ACTIVE_STATUS_ID = 2;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session s = sf.openSession();

        try {
            // Load genres
            Criteria c1 = s.createCriteria(Genre.class);
            List<Genre> genreList = c1.list();

            // Load movies with active status
            Status status = (Status) s.get(Status.class, ACTIVE_STATUS_ID);
            Criteria c2 = s.createCriteria(Movie.class);
            c2.addOrder(Order.desc("id"));
            c2.add(Restrictions.eq("status", status));
            responseObject.addProperty("allMovieCount", c2.list().size());

            c2.setFirstResult(0);
            c2.setMaxResults(10);
            List<Movie> movieList = c2.list();

            for (Movie movie : movieList) {
                movie.setActors(null);
                movie.setDirectors(null);
                movie.setGenres(null);
                movie.setPlotKeywords(null);
            }

            Gson gson = new Gson();
            responseObject.add("genreList", gson.toJsonTree(genreList));
            responseObject.add("movieList", gson.toJsonTree(movieList));
            responseObject.addProperty("status", true);

        } catch (HibernateException e) {
            responseObject.addProperty("message", "Error: " + e.getMessage());
        } finally {
            s.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(responseObject));
    }
}