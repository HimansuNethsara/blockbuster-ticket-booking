package controller;

import com.google.gson.JsonObject;
import hibernate.Cinema;
import hibernate.Hall;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

@MultipartConfig
@WebServlet(name = "SaveHall", urlPatterns = {"/SaveHall"})
public class SaveHall extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        JsonObject jsonResponse = new JsonObject();

        String name = request.getParameter("name");
        String seatsStr = request.getParameter("seats");
        String cinemaIdStr = request.getParameter("cinemaId");

        // Validation
        if (name == null || name.trim().isEmpty()) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Hall name is required.");
            writeResponse(response, jsonResponse);
            return;
        }

        int seats;
        if (seatsStr == null || seatsStr.trim().isEmpty()) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Number of seats is required.");
            writeResponse(response, jsonResponse);
            return;
        }
        try {
            seats = Integer.parseInt(seatsStr);
            if (seats <= 0) {
                jsonResponse.addProperty("status", false);
                jsonResponse.addProperty("message", "Number of seats must be greater than 0.");
                writeResponse(response, jsonResponse);
                return;
            }
        } catch (NumberFormatException e) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Invalid number format for seats.");
            writeResponse(response, jsonResponse);
            return;
        }

        int cinemaId;
        if (cinemaIdStr == null || cinemaIdStr.trim().isEmpty()) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Cinema selection is required.");
            writeResponse(response, jsonResponse);
            return;
        }
        try {
            cinemaId = Integer.parseInt(cinemaIdStr);
            if (cinemaId == 0) {
                jsonResponse.addProperty("status", false);
                jsonResponse.addProperty("message", "Please select a valid cinema.");
                writeResponse(response, jsonResponse);
                return;
            }
        } catch (NumberFormatException e) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Invalid cinema ID.");
            writeResponse(response, jsonResponse);
            return;
        }

        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            Cinema cinema = (Cinema) session.get(Cinema.class, cinemaId);
            if (cinema == null) {
                jsonResponse.addProperty("status", false);
                jsonResponse.addProperty("message", "Selected cinema not found.");
            } else {
                Hall hall = new Hall();
                hall.setName(name);
                hall.setTotalSeats(seats);
                hall.setCinema(cinema);

                session.save(hall);
                tx.commit();

                jsonResponse.addProperty("status", true);
                jsonResponse.addProperty("message", "Hall added successfully.");
            }
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Error saving hall.");
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
