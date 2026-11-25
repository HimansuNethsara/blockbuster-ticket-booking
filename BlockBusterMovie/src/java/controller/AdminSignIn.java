package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Admin;
import hibernate.HibernateUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import org.hibernate.*;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "AdminSignIn", urlPatterns = {"/AdminSignIn"})
public class AdminSignIn extends HttpServlet {

   
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject user = gson.fromJson(request.getReader(), JsonObject.class);

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false); // Default

        String username = user.get("username").getAsString();
        String password = user.get("password").getAsString();

        if (username.isEmpty()) {
            responseObject.addProperty("message", "Username cannot be empty!");
        } else if (password.isEmpty()) {
            responseObject.addProperty("message", "Password cannot be empty!");
        } else {
            SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
            Session s = sessionFactory.openSession();

            Criteria c = s.createCriteria(Admin.class);
            c.add(Restrictions.eq("username", username));
            c.add(Restrictions.eq("password", password));

            Admin admin = (Admin) c.uniqueResult();
            if (admin != null) {
    System.out.println("Admin found: " + admin.getUsername());
    
} else {
    System.out.println("No admin matched!");
}


            if (admin != null) {
                HttpSession httpSession = request.getSession();
                httpSession.setAttribute("admin", admin);

                responseObject.addProperty("status", true);
                responseObject.addProperty("message", "Login successful");
            } else {
                responseObject.addProperty("message", "Invalid username or password");
            }

            s.close();
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
        
    }
    
}