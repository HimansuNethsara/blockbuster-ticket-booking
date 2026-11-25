package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.User;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import model.Mail;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "ForgotPassword", urlPatterns = {"/ForgotPassword"})
public class ForgotPassword extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject jsonRequest = gson.fromJson(request.getReader(), JsonObject.class);
        String email = jsonRequest.get("email").getAsString();

        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("status", false);

        if (email == null || email.isEmpty()) {
            jsonResponse.addProperty("message", "Please enter your registered email.");
        } else {
            SessionFactory factory = HibernateUtil.getSessionFactory();
            Session session = factory.openSession();

            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Restrictions.eq("email", email));
            List<User> users = criteria.list();

            if (users.isEmpty()) {
                jsonResponse.addProperty("message", "This email is not registered.");
            } else {
                User user = users.get(0);
                String password = user.getPassword(); 

                String htmlContent = generateEmail(user.getFirst_name(), password);

                new Thread(() -> {
                    Mail.sendMail(email, "Your BlockBuster Password", htmlContent);
                }).start();

                jsonResponse.addProperty("status", true);
                jsonResponse.addProperty("message", "Password has been sent to your email.");
            }
            session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(jsonResponse));
    }

    private String generateEmail(String firstName, String password) {
        return "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Roboto, Arial, sans-serif; background-color: #f2f2f2; margin: 0; padding: 0; }" +
                ".email-wrapper { max-width: 600px; margin: 30px auto; background-color: #ffffff; border-radius: 10px;" +
                "overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }" +
                ".header { background-color: #d90429; padding: 20px 30px; color: white; text-align: center; }" +
                ".header h1 { margin: 0; font-size: 24px; }" +
                ".content { padding: 30px; color: #333333; font-size: 16px; line-height: 1.6; }" +
                ".password-box { background-color: #ffe8e8; border: 1px dashed #d90429; color: #d90429;" +
                "font-weight: bold; text-align: center; font-size: 20px; padding: 15px; border-radius: 6px; margin: 20px 0; }" +
                ".footer { background-color: #f9f9f9; text-align: center; padding: 15px; font-size: 13px; color: #999999; }" +
                "</style></head><body>" +
                "<div class='email-wrapper'>" +
                "<div class='header'><h1>BlockBuster – Password Recovery</h1></div>" +
                "<div class='content'>" +
                "<p>Hi " + firstName + ",</p>" +
                "<p>We received a request to recover your password for your BlockBuster account.</p>" +
                "<p>Here is your password:</p>" +
                "<div class='password-box'>" + password + "</div>" +
                "<p>If you didn’t request this, you can safely ignore this email. No changes have been made to your account.</p>" +
                "</div>" +
                "<div class='footer'>&copy; 2025 BlockBuster. All rights reserved.</div>" +
                "</div></body></html>";
    }


}
