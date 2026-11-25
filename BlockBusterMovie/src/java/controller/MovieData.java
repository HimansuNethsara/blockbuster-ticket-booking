package controller;

import com.google.gson.Gson;
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
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;

@WebServlet(name = "MovieData", urlPatterns = {"/MovieData"})
public class MovieData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        Session session = null;
        Transaction tx = null;

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (PrintWriter out = response.getWriter()) {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(Movie.class);
            criteria.addOrder(Order.asc("title"));

            List<Movie> movieList = criteria.list();

            tx.commit();

            Gson gson = new Gson();
            String movieJson = gson.toJson(movieList);

            out.write(movieJson);

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading movie data");
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

}
