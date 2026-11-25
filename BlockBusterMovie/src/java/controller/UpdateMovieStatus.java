package controller;

import com.google.gson.JsonObject;
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
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

@WebServlet(name = "UpdateMovieStatus", urlPatterns = {"/UpdateMovieStatus"})
public class UpdateMovieStatus extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        Session session = null;
        Transaction tx = null;
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();
        JsonObject json = new JsonObject();

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            int movieId = Integer.parseInt(request.getParameter("id"));
            int statusId = Integer.parseInt(request.getParameter("status"));

            Movie movie = (Movie) session.get(Movie.class, movieId);
            Status newStatus = (Status) session.get(Status.class, statusId);

            if (movie != null && newStatus != null) {
                movie.setStatus(newStatus);
                session.update(movie);
                tx.commit();

                json.addProperty("status", true);
                json.addProperty("message", "Movie status updated to " + newStatus.getValue());
            } else {
                json.addProperty("status", false);
                json.addProperty("message", "Invalid movie or status");
            }

        } catch (NumberFormatException | HibernateException e) {
            if (tx != null) tx.rollback();
            json.addProperty("status", false);
            json.addProperty("message", "Error: " + e.getMessage());
        } finally {
            if (session != null) session.close();
            out.print(json.toString());
            
        }

    }

}
