package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Actors;
import hibernate.HibernateUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.hibernate.Session;

@MultipartConfig
@WebServlet(name = "SaveActor", urlPatterns = {"/SaveActor"})
public class SaveActor extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        JsonObject json = new JsonObject();
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            session.beginTransaction();

            String name = request.getParameter("name");
            Part imagePart = request.getPart("image");

            if (name == null || name.trim().isEmpty()) {
                json.addProperty("status", false);
                json.addProperty("message", "Actor name is required.");
                sendJson(response, json);
                return;
            }

            if (imagePart == null || imagePart.getSize() == 0) {
                json.addProperty("status", false);
                json.addProperty("message", "Actor image is required.");
                sendJson(response, json);
                return;
            }
            
            Actors existing = (Actors) session.createQuery("FROM Actors WHERE name = :name")
                                  .setParameter("name", name.trim())
                                  .uniqueResult();

            if (existing != null) {
                json.addProperty("status", false);
                json.addProperty("message", "An actor with this name already exists.");
                sendJson(response, json);
                return;
            }

            Actors actor = new Actors();
            actor.setName(name.trim());
            session.save(actor);
            session.getTransaction().commit();

            String appPath = getServletContext().getRealPath("");
            String basePath = appPath.replace("build" + File.separator + "web", "web" + File.separator + "actor-images");
            File actorDir = new File(basePath, String.valueOf(actor.getId()));
            if (!actorDir.exists()) actorDir.mkdirs();

            File imageFile = new File(actorDir, "actor.png");
            Files.copy(imagePart.getInputStream(), imageFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            json.addProperty("status", true);
            json.addProperty("message", "Actor added successfully.");
            sendJson(response, json);

        } catch (IOException | ServletException e) {
            if (session.getTransaction().isActive()) session.getTransaction().rollback();
            json.addProperty("status", false);
            json.addProperty("message", "Error: " + e.getMessage());
            sendJson(response, json);
        } finally {
            session.close();
        }
    }

    private void sendJson(HttpServletResponse response, JsonObject obj) throws IOException {
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(obj));
    }

}
