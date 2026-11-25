package controller;

import com.google.gson.JsonObject;
import hibernate.Cinema;
import hibernate.Hall;
import hibernate.HibernateUtil;
import hibernate.Movie;
import hibernate.Showtime;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.Time;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

@MultipartConfig
@WebServlet(name = "SaveShowTime", urlPatterns = {"/SaveShowTime"})
public class SaveShowTime extends HttpServlet {

     @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        JsonObject jsonResponse = new JsonObject();

        String movieIdStr = request.getParameter("movieId");
        String cinemaIdStr = request.getParameter("cinemaId");
        String hallIdStr = request.getParameter("hallId");
        String dateStr = request.getParameter("showDate");
        String timeStr = request.getParameter("showTime");
        String priceStr = request.getParameter("price");

        // Validation
        int movieId;
        if (movieIdStr == null || movieIdStr.equals("0")) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Please select a movie.");
            writeResponse(response, jsonResponse);
            return;
        }
        try {
            movieId = Integer.parseInt(movieIdStr);
        } catch (NumberFormatException e) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Invalid movie ID.");
            writeResponse(response, jsonResponse);
            return;
        }
        
        int cinemaId;
        if (cinemaIdStr == null || cinemaIdStr.equals("0")) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Please select a cinema.");
            writeResponse(response, jsonResponse);
            return;
        }
        try {
            cinemaId = Integer.parseInt(cinemaIdStr);
        } catch (NumberFormatException e) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Invalid cinema ID.");
            writeResponse(response, jsonResponse);
            return;
        }

        int hallId;
        if (hallIdStr == null || hallIdStr.equals("0")) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Please select a hall.");
            writeResponse(response, jsonResponse);
            return;
        }
        try {
            hallId = Integer.parseInt(hallIdStr);
        } catch (NumberFormatException e) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Invalid hall ID.");
            writeResponse(response, jsonResponse);
            return;
        }

        if (dateStr == null || dateStr.trim().isEmpty()) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Please select a show date.");
            writeResponse(response, jsonResponse);
            return;
        }

        if (timeStr == null || timeStr.trim().isEmpty()) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Please select a show time.");
            writeResponse(response, jsonResponse);
            return;
        }
        
        try {
            LocalDate showDate = LocalDate.parse(dateStr);
            LocalTime showTime = LocalTime.parse(timeStr);

            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            if (showDate.isBefore(today)) {
                jsonResponse.addProperty("status", false);
                jsonResponse.addProperty("message", "Show date cannot be in the past.");
                writeResponse(response, jsonResponse);
                return;
            }

            if (showDate.isEqual(today) && showTime.isBefore(now)) {
                jsonResponse.addProperty("status", false);
                jsonResponse.addProperty("message", "Show time cannot be in the past.");
                writeResponse(response, jsonResponse);
                return;
            }
        } catch (DateTimeParseException e) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Invalid date or time format.");
            writeResponse(response, jsonResponse);
            return;
        }

        double price;
        if (priceStr == null || priceStr.trim().isEmpty()) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Please enter a ticket price.");
            writeResponse(response, jsonResponse);
            return;
        }
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                jsonResponse.addProperty("status", false);
                jsonResponse.addProperty("message", "Ticket price must be greater than 0.");
                writeResponse(response, jsonResponse);
                return;
            }
        } catch (NumberFormatException e) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Invalid ticket price.");
            writeResponse(response, jsonResponse);
            return;
        }

        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            Movie movie = (Movie) session.get(Movie.class, movieId);
            Cinema cinema = (Cinema) session.get(Cinema.class, cinemaId);
            Hall hall = (Hall) session.get(Hall.class, hallId);

            if (movie == null || hall == null || cinema == null) {
                jsonResponse.addProperty("status", false);
                jsonResponse.addProperty("message", "Invalid movie or hall or cinema.");
            } else {
                Showtime showtime = new Showtime();
                showtime.setMovie(movie);
                showtime.setHall(hall);
                showtime.setCinema(cinema); 
                showtime.setShowDate(Date.valueOf(dateStr));
                showtime.setShowTime(Time.valueOf(timeStr + ":00"));
                showtime.setPrice(price);

                session.save(showtime);
                tx.commit();

                jsonResponse.addProperty("status", true);
                jsonResponse.addProperty("message", "Showtime added successfully.");
            }
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Error saving showtime.");
        } finally {
            if (session != null) session.close();
        }

        writeResponse(response, jsonResponse);
    }

    private void writeResponse(HttpServletResponse response, JsonObject jsonResponse) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }

}
