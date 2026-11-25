package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Genre;
import hibernate.HibernateUtil;
import hibernate.Movie;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "LoadSingleMovie", urlPatterns = {"/LoadSingleMovie"})
public class LoadSingleMovie extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false); 

        String movieId = request.getParameter("id");
        if (Util.isInteger(movieId)) {
            SessionFactory sf = HibernateUtil.getSessionFactory();
            Session session = sf.openSession();
            try {
                Movie movie = (Movie) session.get(Movie.class, Integer.valueOf(movieId));

                    if (movie != null) {
                        if (movie.getStatus() != null && "Active".equals(movie.getStatus().getValue())) {

                            Criteria criteria = session.createCriteria(Movie.class, "m");

                            criteria.createAlias("m.status", "s");

                            criteria.add(Restrictions.eq("m.releaseYear", movie.getReleaseYear()));
                            criteria.add(Restrictions.ne("m.id", movie.getId()));
                            criteria.add(Restrictions.eq("s.value", "Active")); 

                            criteria.setMaxResults(6);
                            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                            List<Movie> similarMovies = criteria.list();


                            responseObject.add("movie", gson.toJsonTree(movie));
                            responseObject.add("similarMovies", gson.toJsonTree(similarMovies));
                            responseObject.addProperty("status", true);

                        } else {
                            responseObject.addProperty("message", "Movie is not Active");
                        }

                    } else {
                        responseObject.addProperty("message", "Movie Not Found");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    responseObject.addProperty("message", "Server error occurred");
                } finally {
                    session.close();
                }
            } else {
                responseObject.addProperty("message", "Invalid movie ID");
            }

            String toJson = gson.toJson(responseObject);
            response.setContentType("application/json");
            response.getWriter().write(toJson);
        }

}
