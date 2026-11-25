package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.Cinema;
import hibernate.City;
import hibernate.HibernateUtil;
import hibernate.Showtime;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;

@MultipartConfig
@WebServlet(name = "SaveCinema", urlPatterns = {"/SaveCinema"})
public class SaveCinema extends HttpServlet {

     @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String name = request.getParameter("name");
        String cityIdStr = request.getParameter("cityId");

        JsonObject jsonResponse = new JsonObject();

        if (name == null || name.trim().isEmpty() || cityIdStr == null) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Cinema name or city ID missing.");
            response.setContentType("application/json");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        int cityId;
        try {
            cityId = Integer.parseInt(cityIdStr);
        } catch (NumberFormatException e) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Invalid city ID.");
            response.setContentType("application/json");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        Session session = null;
        Transaction tx = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            City city = (City) session.get(City.class, cityId);
            if (city == null) {
                jsonResponse.addProperty("status", false);
                jsonResponse.addProperty("message", "City not found.");
            } else {
                Cinema cinema = new Cinema();
                cinema.setName(name);
                cinema.setCity(city);

                session.save(cinema);
                tx.commit();

                jsonResponse.addProperty("status", true);
                jsonResponse.addProperty("message", "Cinema added successfully.");
            }

        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Error saving cinema.");
        } finally {
            if (session != null) session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }
    
}
