package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.Movie;
import java.io.IOException;
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

@WebServlet(name = "LoadAllMovies", urlPatterns = {"/LoadAllMovies"})
public class LoadAllMovies extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        JsonArray movieArray = new JsonArray();

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session session = sf.openSession();

        try {
            Criteria criteria = session.createCriteria(Movie.class, "m")
                .createAlias("m.status", "s")
                .createAlias("m.mpaaRating", "mpaa")
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                .addOrder(Order.asc("m.id"));

            List<Movie> movies = criteria.list();

            for (Movie movie : movies) {
                JsonObject data = new JsonObject();
                data.addProperty("id", movie.getId());
                data.addProperty("title", movie.getTitle());
                data.addProperty("imdb_rating", movie.getImdbRating());
                data.addProperty("rt_rating", movie.getRtRating());
                data.addProperty("release_year", movie.getReleaseYear());
                data.addProperty("runtime", movie.getRuntime());
                data.addProperty("mpaa_rating", movie.getMpaaRating().getValue());
                data.addProperty("statusId", movie.getStatus().getId());  
                data.addProperty("status", movie.getStatus().getValue());

                // Poster path
                String posterPath = request.getContextPath() + "/movie-posters/" + movie.getId() + "/poster.png";
                data.addProperty("poster_url", posterPath);

                movieArray.add(data);
            }
        } catch (HibernateException e) {
        } finally {
            session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(movieArray));
    }    

}
