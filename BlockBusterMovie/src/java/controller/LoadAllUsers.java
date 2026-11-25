package controller;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

@WebServlet(name = "LoadAllUsers", urlPatterns = {"/LoadAllUsers"})
public class LoadAllUsers extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = session.createCriteria(User.class);
            criteria.addOrder(Order.asc("id")); 
            List<User> users = criteria.list();

            List<Map<String, String>> userDataList = new ArrayList<>();
            for (User u : users) {
                Map<String, String> data = new HashMap<>();
                data.put("id", String.valueOf(u.getId()));
                data.put("first_name", u.getFirst_name());
                data.put("last_name", u.getLast_name());
                data.put("email", u.getEmail());
                data.put("status", u.getVerification());
                data.put("joined", u.getCreated_at().toString()); 
                userDataList.add(data);
            }

            response.setContentType("application/json");
            new Gson().toJson(userDataList, response.getWriter());

        } catch (JsonIOException | IOException | HibernateException e) {
            response.sendError(500, "Internal Server Error");
        } finally {
            if (session != null) session.close();
        }
    }
    
}
