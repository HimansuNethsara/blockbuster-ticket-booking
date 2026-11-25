package controller;

import com.google.gson.Gson;
import hibernate.Cinema;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.Session;

@WebServlet(name = "CinemaData", urlPatterns = {"/CinemaData"})
public class CinemaData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        Session session = HibernateUtil.getSessionFactory().openSession();
        Criteria c = session.createCriteria(Cinema.class);
        List<Cinema> cinemaList = c.list();
        session.close();

        Gson gson = new Gson();
        String toJson = gson.toJson(cinemaList);
        response.setContentType("application/json");
        response.getWriter().write(toJson);
    }

}
