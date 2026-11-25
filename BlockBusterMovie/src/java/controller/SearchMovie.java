package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.Movie;
import hibernate.Status;
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
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "SearchMovie", urlPatterns = {"/SearchMovie"})
public class SearchMovie extends HttpServlet {

    private static final int PAGE_SIZE = 8;  // movies per page
    private static final int ACTIVE_ID = 2;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        JsonObject requestJson = gson.fromJson(request.getReader(), JsonObject.class);

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session session = sf.openSession();

        try {
            Criteria criteria = session.createCriteria(Movie.class);

            if (requestJson.has("title")) {
                String title = requestJson.get("title").getAsString();
                if (!title.isEmpty()) {
                    criteria.add(Restrictions.ilike("title", "%" + title + "%"));
                }
            }

            if (requestJson.has("genres")) {
                JsonArray genreIdsJson = requestJson.getAsJsonArray("genres");
                if (genreIdsJson.size() > 0) {
                    List<Integer> genreIds = new ArrayList<>();
                    for (JsonElement elem : genreIdsJson) {
                        genreIds.add(elem.getAsInt());
                    }

                    criteria.createAlias("genres", "g");
                    criteria.add(Restrictions.in("g.id", genreIds));
                    criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY); 
                }
            }

            if (requestJson.has("imdbFrom") && requestJson.has("imdbTo")) {
                float imdbFrom = requestJson.get("imdbFrom").getAsFloat();
                float imdbTo = requestJson.get("imdbTo").getAsFloat();
                criteria.add(Restrictions.ge("imdbRating", imdbFrom));
                criteria.add(Restrictions.le("imdbRating", imdbTo));
            }

            if (requestJson.has("yearFrom") && requestJson.has("yearTo")) {
                int yearFrom = requestJson.get("yearFrom").getAsInt();
                int yearTo = requestJson.get("yearTo").getAsInt();
                criteria.add(Restrictions.ge("releaseYear", yearFrom));
                criteria.add(Restrictions.le("releaseYear", yearTo));
            }

            // Pagination
            int firstResult = 0;
            if (requestJson.has("firstResult")) {
                firstResult = requestJson.get("firstResult").getAsInt();
            }
            criteria.setFirstResult(firstResult);
            criteria.setMaxResults(PAGE_SIZE);

            Status activeStatus = (Status) session.get(Status.class, SearchMovie.ACTIVE_ID); 
            criteria.add(Restrictions.eq("status", activeStatus));
            
            // Sorting
            if (requestJson.has("sortOption")) {
                String sortOption = requestJson.get("sortOption").getAsString();
                switch (sortOption) {
                    case "rating_desc":
                        criteria.addOrder(Order.desc("imdbRating"));
                        break;
                    case "rating_asc":
                        criteria.addOrder(Order.asc("imdbRating"));
                        break;
                    case "date_desc":
                        criteria.addOrder(Order.desc("releaseYear")); 
                        break;
                    case "date_asc":
                        criteria.addOrder(Order.asc("releaseYear"));
                        break;
                    default:
                        criteria.addOrder(Order.desc("id")); 
                }
            }

            List<Movie> movies = criteria.list();

            Criteria countCriteria = session.createCriteria(Movie.class);
            
            if (requestJson.has("title")) {
                String title = requestJson.get("title").getAsString();
                if (!title.isEmpty()) {
                    countCriteria.add(Restrictions.ilike("title", "%" + title + "%"));
                }
            }
            if (requestJson.has("genres")) {
                JsonArray genreIdsJson = requestJson.getAsJsonArray("genres");
                if (genreIdsJson.size() > 0) {
                    List<Integer> genreIds = new ArrayList<>();
                    for (JsonElement elem : genreIdsJson) {
                        genreIds.add(elem.getAsInt());
                    }
                    countCriteria.createAlias("genres", "g");
                    countCriteria.add(Restrictions.in("g.id", genreIds));
                    countCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
                }
            }
            if (requestJson.has("imdbFrom") && requestJson.has("imdbTo")) {
                float imdbFrom = requestJson.get("imdbFrom").getAsFloat();
                float imdbTo = requestJson.get("imdbTo").getAsFloat();
                countCriteria.add(Restrictions.ge("imdbRating", imdbFrom));
                countCriteria.add(Restrictions.le("imdbRating", imdbTo));
            }
            if (requestJson.has("yearFrom") && requestJson.has("yearTo")) {
                int yearFrom = requestJson.get("yearFrom").getAsInt();
                int yearTo = requestJson.get("yearTo").getAsInt();
                countCriteria.add(Restrictions.ge("releaseYear", yearFrom));
                countCriteria.add(Restrictions.le("releaseYear", yearTo));
            }

            List<Movie> allMatched = countCriteria.list();
            int totalCount = allMatched.size();

            JsonArray movieArray = gson.toJsonTree(movies).getAsJsonArray();

            for (JsonElement elem : movieArray) {
                JsonObject movieObj = elem.getAsJsonObject();
                int movieId = movieObj.get("id").getAsInt();

                String imagePath = "movie-posters/" + movieId + "/poster.png"; 
                movieObj.addProperty("poster", imagePath);

                movieObj.remove("actors");
                movieObj.remove("directors");
                movieObj.remove("genres");
                movieObj.remove("plotKeywords");
            }

            responseObject.add("movies", movieArray);
            responseObject.addProperty("totalCount", totalCount);
            responseObject.addProperty("status", true);

        } catch (Exception e) {
            responseObject.addProperty("message", "Error: " + e.getMessage());
        } finally {
            session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
    }
}
    


