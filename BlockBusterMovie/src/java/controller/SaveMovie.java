package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Actors;
import hibernate.Directors;
import hibernate.Genre;
import hibernate.HibernateUtil;
import hibernate.Keyword;
import hibernate.Movie;
import hibernate.MpaaRating;
import hibernate.Status;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.hibernate.Session;

@MultipartConfig
@WebServlet(name = "SaveMovie", urlPatterns = {"/SaveMovie"})
public class SaveMovie extends HttpServlet {

    private static final int DEFAULT_STATUS_ID = 2;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            session.beginTransaction();

            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String imdbStr = request.getParameter("imdb");
            String rtRatingStr = request.getParameter("rt_rating");
            String trailerUrl = request.getParameter("trailer");
            String yearStr = request.getParameter("year");
            String runtimeStr = request.getParameter("runtime");
            String mpaaRatingStr = request.getParameter("mpaaRating");


            if (title == null || title.trim().isEmpty()) {
                responseObject.addProperty("message", "Title is required.");
                sendJson(response, responseObject);
                return;
            }
            if (description == null || description.trim().isEmpty()) {
                responseObject.addProperty("message", "Description is required.");
                sendJson(response, responseObject);
                return;
            }
            
            
            if (imdbStr == null || imdbStr.trim().isEmpty()) {
                responseObject.addProperty("message", "IMDb rating is required.");
                sendJson(response, responseObject);
                return;
            }

            float imdb;
            try {
                imdb = Float.parseFloat(imdbStr);
                if (imdb < 0.0f || imdb > 10.0f) {
                    responseObject.addProperty("message", "IMDb rating must be between 0 and 10.");
                    sendJson(response, responseObject);
                    return;
                }
            } catch (NumberFormatException e) {
                responseObject.addProperty("message", "Invalid IMDb rating format.");
                sendJson(response, responseObject);
                return;
            }

            if (rtRatingStr == null || rtRatingStr.trim().isEmpty()) {
                responseObject.addProperty("message", "Rotten Tomatoes rating is required.");
                sendJson(response, responseObject);
                return;
            }

            float rt;
            try {
                rt = Float.parseFloat(rtRatingStr);
                if (rt < 0.0f || rt > 100.0f) {
                    responseObject.addProperty("message", "Rotten Tomatoes rating must be between 0 and 100.");
                    sendJson(response, responseObject);
                    return;
                }
            } catch (NumberFormatException e) {
                responseObject.addProperty("message", "Invalid Rotten Tomatoes rating format.");
                sendJson(response, responseObject);
                return;
            }


            if (mpaaRatingStr == null || mpaaRatingStr.trim().isEmpty()) {
                responseObject.addProperty("message", "MPAA rating is required.");
                sendJson(response, responseObject);
                return;
            }

            MpaaRating mpaaRating;

            try {
                int mpaaRatingId = Integer.parseInt(mpaaRatingStr);
                mpaaRating = (MpaaRating) session.get(MpaaRating.class, mpaaRatingId);
                if (mpaaRating == null) {
                    responseObject.addProperty("message", "Invalid MPAA rating.");
                    sendJson(response, responseObject);
                    return;
                }
            } catch (NumberFormatException e) {
                responseObject.addProperty("message", "MPAA rating must be a valid number.");
                sendJson(response, responseObject);
                return;
            }

            String[] genreIds = request.getParameterValues("genres");
            String[] actorIds = request.getParameterValues("actors");
            String[] directorIds = request.getParameterValues("directors");
            String[] keywordIds = request.getParameterValues("keywords");

            if (genreIds == null || genreIds.length == 0) {
                responseObject.addProperty("message", "At least one genre must be selected.");
                sendJson(response, responseObject);
                return;
            }
            if (actorIds == null || actorIds.length == 0) {
                responseObject.addProperty("message", "At least one actor must be selected.");
                sendJson(response, responseObject);
                return;
            }
            if (directorIds == null || directorIds.length == 0) {
                responseObject.addProperty("message", "At least one director must be selected.");
                sendJson(response, responseObject);
                return;
            }
            if (keywordIds == null || keywordIds.length == 0) {
                responseObject.addProperty("message", "At least one keyword must be selected.");
                sendJson(response, responseObject);
                return;
            }
            
            // Poster image validation
            Part posterPart = request.getPart("image1");

            if (posterPart == null || posterPart.getSize() == 0) {
                responseObject.addProperty("message", "Poster image is required.");
                response.setContentType("application/json");
                response.getWriter().write(new Gson().toJson(responseObject));
                return;
            }

            String fileName = posterPart.getSubmittedFileName();

            if (fileName == null || fileName.trim().isEmpty()) {
                responseObject.addProperty("message", "Invalid poster file.");
                response.setContentType("application/json");
                response.getWriter().write(new Gson().toJson(responseObject));
                return;
            }
            
            if (trailerUrl == null || trailerUrl.trim().isEmpty()) {
                responseObject.addProperty("message", "Trailer URL is required.");
                sendJson(response, responseObject);
                return;
            }
            
            int releaseYear;
            try {
                releaseYear = Integer.parseInt(yearStr);
                if (releaseYear < 1888 || releaseYear > 2100) {
                    responseObject.addProperty("message", "Release year must be between 1888 and 2100.");
                    sendJson(response, responseObject);
                    return;
                }
            } catch (NumberFormatException e) {
                responseObject.addProperty("message", "Release year must be a valid number.");
                sendJson(response, responseObject);
                return;
            }

            // Runtime validation
            int runtime;
            try {
                runtime = Integer.parseInt(runtimeStr);
                if (runtime < 1 || runtime > 500) {
                    responseObject.addProperty("message", "Runtime must be between 1 and 500 minutes.");
                    sendJson(response, responseObject);
                    return;
                }
            } catch (NumberFormatException e) {
                responseObject.addProperty("message", "Runtime must be a valid number.");
                sendJson(response, responseObject);
                return;
            }
            
            List<Genre> genres = new ArrayList<>();
            for (String id : genreIds) {
                Genre g = (Genre) session.get(Genre.class, Integer.parseInt(id));
                if (g != null) genres.add(g);
            }

            List<Actors> actors = new ArrayList<>();
            for (String id : actorIds) {
                Actors a = (Actors) session.get(Actors.class, Integer.parseInt(id));
                if (a != null) actors.add(a);
            }

            List<Directors> directors = new ArrayList<>();
            for (String id : directorIds) {
                Directors d = (Directors) session.get(Directors.class, Integer.parseInt(id));
                if (d != null) directors.add(d);
            } 

            List<Keyword> keywords = new ArrayList<>();
            for (String id : keywordIds) {
                Keyword k = (Keyword) session.get(Keyword.class, Integer.parseInt(id));
                if (k != null) keywords.add(k);
            }
            
            Movie movie = new Movie();
            movie.setTitle(title);
            movie.setDescription(description);
            movie.setTrailerUrl(trailerUrl);
            movie.setReleaseYear(releaseYear);
            movie.setRuntime(runtime);
            movie.setMpaaRating(mpaaRating);
            movie.setImdbRating(imdb);
            movie.setRtRating(rt);
            movie.setGenres(genres);
            movie.setActors(actors);
            movie.setDirectors(directors);
            movie.setPlotKeywords(keywords);
            
            Status status = (Status) session.get(Status.class, DEFAULT_STATUS_ID);
            movie.setStatus(status);


            // Save movie
            session.save(movie);
            session.getTransaction().commit();
            
            // Save poster
            String appPath = getServletContext().getRealPath("");
            if (appPath == null || appPath.trim().isEmpty()) {
                responseObject.addProperty("message", "Server error: unable to resolve path for saving image.");
                sendJson(response, responseObject);
                return;
            }

            String basePath = appPath.replace("build" + File.separator + "web", "web" + File.separator + "movie-posters");
            File movieFolder = new File(basePath, String.valueOf(movie.getId()));
            if (!movieFolder.exists()) movieFolder.mkdirs();

            File posterFile = new File(movieFolder, "poster.png");
            Files.copy(posterPart.getInputStream(), posterFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            responseObject.addProperty("status", true);
            responseObject.addProperty("message", "Movie saved successfully.");

        } catch (IOException | NumberFormatException | ServletException e) {
            if (session.getTransaction().isActive()) session.getTransaction().rollback();
            responseObject.addProperty("message", "Error: " + e.getMessage());
        } finally {
            session.close();
        }

        sendJson(response, responseObject);
    }

    private void sendJson(HttpServletResponse response, JsonObject obj) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(obj));
    }
}
