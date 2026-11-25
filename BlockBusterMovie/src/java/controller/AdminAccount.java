package controller;

import com.google.gson.JsonObject;
import hibernate.Admin;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "AdminAccount", urlPatterns = {"/AdminAccount"})
public class AdminAccount extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("admin") != null) {
            Admin admin = (Admin) session.getAttribute("admin");

            JsonObject json = new JsonObject();
            json.addProperty("username", admin.getUsername());
            json.addProperty("email", admin.getEmail());

            response.setContentType("application/json");
            response.getWriter().write(json.toString());
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
    

}
