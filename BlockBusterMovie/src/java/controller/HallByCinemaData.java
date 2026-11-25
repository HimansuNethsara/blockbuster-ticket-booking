package controller;

import com.google.gson.Gson;
import hibernate.Hall;
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
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "HallByCinemaData", urlPatterns = {"/HallByCinemaData"})
public class HallByCinemaData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String cinemaIdStr = request.getParameter("cinemaId");

        if (cinemaIdStr == null) {
            response.setContentType("application/json");
            response.getWriter().write("[]");
            return;
        }

        int cinemaId;
        try {
            cinemaId = Integer.parseInt(cinemaIdStr);
        } catch (NumberFormatException e) {
            response.setContentType("application/json");
            response.getWriter().write("[]");
            return;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Criteria c = session.createCriteria(Hall.class);
        c.createAlias("cinema", "cin");
        c.add(Restrictions.eq("cin.id", cinemaId));

        List<Hall> hallList = c.list();
        session.close();

        Gson gson = new Gson();
        String toJson = gson.toJson(hallList);
        response.setContentType("application/json");
        response.getWriter().write(toJson);
    }

}
