package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "SignIn", urlPatterns = {"/SignIn"})
public class SignIn extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject user = gson.fromJson(request.getReader(), JsonObject.class);
        
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        final String email = user.get("email").getAsString();
        String password = user.get("password").getAsString();
        
        if (email.isEmpty()) {
            responseObject.addProperty("message", "Email cannot be empty!");
        } else if (!Util.isEmailValid(email)) {
            responseObject.addProperty("message", "Please enter a valid email");
        } else if (password.isEmpty()) {
            responseObject.addProperty("message", "Password cannot be empty!");
        }else {
        
            SessionFactory sessionFactory =  HibernateUtil.getSessionFactory();
            Session s = sessionFactory.openSession();
            
            Criteria c = s.createCriteria(User.class);
            
            Criterion crt1 = Restrictions.eq("email", email);
            Criterion crt2 = Restrictions.eq("password", password);
            
            c.add(crt1);
            c.add(crt2);
            
            if (c.list().isEmpty()) {
                responseObject.addProperty("message", "Invalid verification code");
            }else{
            
                User u = (User) c.list().get(0);
                responseObject.addProperty("status", true);
                
                HttpSession ses = request.getSession();

                if (!u.getVerification().equals("Verified")) {//not verified

                    ses.setAttribute("email", email);
                    responseObject.addProperty("message", "1");               
                    
                }else {//verified
                
                    ses.setAttribute("user", u);
                    responseObject.addProperty("message", "2");  
                    
                }                
                
            }
            
            s.close();
            
        }
            
        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }
 
}