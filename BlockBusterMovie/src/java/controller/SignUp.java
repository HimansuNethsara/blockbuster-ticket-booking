package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.HibernateUtil;
import hibernate.User;
import hibernate.UserDAO;
import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Mail;
import model.Util;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "SignUp", urlPatterns = {"/SignUp"})
public class SignUp extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject user = gson.fromJson(request.getReader(), JsonObject.class);

        String firstName = user.get("firstName").getAsString();
        String lastName = user.get("lastName").getAsString();
        final String email = user.get("email").getAsString();
        String password = user.get("password").getAsString();

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);
        
        if (firstName == null || firstName.isEmpty()) {
            responseObject.addProperty("message", "First Name cannot be empty!");
        } else if (lastName == null || lastName.isEmpty()) {
            responseObject.addProperty("message", "Last Name cannot be empty!");
        } else if (email == null || email.isEmpty()) {
            responseObject.addProperty("message", "Email cannot be empty!");
        } else if (!Util.isEmailValid(email)) {
            responseObject.addProperty("message", "Please enter a valid email");
        } else if (UserDAO.isEmailRegistered(email)) {
            responseObject.addProperty("message", "This email is already registered!");
        } else if (password == null || password.isEmpty()) {
            responseObject.addProperty("message", "Password cannot be empty!");
        } else if (!Util.isPasswordValid(password)) {
            responseObject.addProperty("message", "This password must contain at least uppercase, lowercase, number, special character and be at least eight characters long!");
        }else {
        
            //hibernate Save
            SessionFactory sessionFactory =  HibernateUtil.getSessionFactory();
            Session s = sessionFactory.openSession();
            
            Criteria criteria = s.createCriteria(User.class);
            criteria.add(Restrictions.eq("email",email));
            
            if (!criteria.list().isEmpty()) {
                responseObject.addProperty("message", "User with this email already exist!");
            }else {
            
                User u =new User();
                u.setFirst_name(firstName);
                u.setLast_name(lastName);
                u.setEmail(email);
                u.setPassword(password);

                final String verificationCode = Util.generateCode();
                u.setVerification(verificationCode);
                u.setCreated_at(new Date());

                s.save(u);
                s.beginTransaction().commit();
                //hibernate Save

                //send Email
                new Thread(() -> {
                    // Full HTML email template as a Java String
                    String htmlTemplate = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        "    <title>BlockBuster Email Verification</title>\n" +
                        "    <style>\n" +
                        "        /* Base Styles */\n" +
                        "        body { font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; line-height: 1.6; color: #333333; background-color: #f7f7f7; margin: 0; padding: 0; }\n" +
                        "        .email-container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }\n" +
                        "        .header { background: linear-gradient(135deg, #e50914, #000000); padding: 30px 20px; text-align: center; color: white; }\n" +
                        "        .header h1 { margin: 0; font-size: 26px; font-weight: 700; }\n" +
                        "        .content { padding: 30px; }\n" +
                        "        .verification-code { background: #f8f9fa; border-radius: 6px; padding: 15px; margin: 25px 0; text-align: center; border: 1px dashed #dee2e6; }\n" +
                        "        .code { font-size: 32px; font-weight: 700; letter-spacing: 3px; color: #e50914; margin: 10px 0; }\n" +
                        "        .footer { text-align: center; padding: 20px; font-size: 12px; color: #6c757d; background: #f8f9fa; border-top: 1px solid #e9ecef; }\n" +
                        "        .note { font-size: 14px; color: #6c757d; margin-top: 20px; }\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <div class=\"email-container\">\n" +
                        "        <div class=\"header\">\n" +
                        "            <h1>Welcome to BlockBuster </h1>\n" +
                        "        </div>\n" +
                        "        <div class=\"content\">\n" +
                        "            <p>Hello Movie Buff,</p>\n" +
                        "            <p>Thanks for joining <strong>BlockBuster</strong> — your ticket to the best movies in town! To complete your sign-up, please verify your email using the code below:</p>\n" +
                        "            <div class=\"verification-code\">\n" +
                        "                <p>Your verification code is:</p>\n" +
                        "                <div class=\"code\">{{VERIFICATION_CODE}}</div>\n" +
                        "                <p>This code will expire in 15 minutes.</p>\n" +
                        "            </div>\n" +
                        "            <p>If you didn’t sign up for BlockBuster, you can safely ignore this email.</p>\n" +
                        "            <p class=\"note\">Please don’t share this code with anyone to keep your account secure.</p>\n" +
                        "        </div>\n" +
                        "        <div class=\"footer\">\n" +
                        "            <p>&copy; 2025 BlockBuster. All rights reserved.</p>\n" +
                        "            <p>Lights. Camera. Action – Anywhere, Anytime!</p>\n" +
                        "        </div>\n" +
                        "    </div>\n" +
                        "</body>\n" +
                        "</html>";

                    String htmlContent = htmlTemplate.replace("{{VERIFICATION_CODE}}", verificationCode);

                    Mail.sendMail(email, "BlockBuster → Email Verification", htmlContent);
                }).start();

                //send Email
                
                //session management
                HttpSession ses = request.getSession();
                ses.setAttribute("email", email);
                //session management
                
                responseObject.addProperty("status", true);
                responseObject.addProperty("message", "Registration success. Please check your email for the verification code");
                
            }
            s.close();
            
        }
        
        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
       
    }

}
