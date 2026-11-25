package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Actors;
import hibernate.Directors;
import hibernate.Genre;
import hibernate.HibernateUtil;
import hibernate.Keyword;
import hibernate.MpaaRating;
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
import org.hibernate.SessionFactory;

@WebServlet(name = "LoadProductData", urlPatterns = {"/LoadProductData"})
public class LoadProductData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);
        
        SessionFactory sf =  HibernateUtil.getSessionFactory();
        Session s = sf.openSession();
        
        //getMpaaRating
        Criteria c1 = s.createCriteria(MpaaRating.class);
        List<MpaaRating> mpaaRatingList = c1.list();
        
        //getMpaaRating
        Criteria c2 = s.createCriteria(Genre.class);
        List<Genre> genreList = c2.list();
        
        //getMpaaRating
        Criteria c3 = s.createCriteria(Actors.class);
        List<Actors> actorList = c3.list();
        
        //getDirector
        Criteria c4 = s.createCriteria(Directors.class);
        List<Directors> directorList = c4.list();
        
        //getKeyword
        Criteria c5 = s.createCriteria(Keyword.class);
        List<Keyword> keywordList = c5.list();
        
        s.close();

        Gson gson = new Gson();
        
        responseObject.add("mpaaRatingList", gson.toJsonTree(mpaaRatingList));
        responseObject.add("genreList", gson.toJsonTree(genreList));
        responseObject.add("actorList", gson.toJsonTree(actorList));
        responseObject.add("directorList", gson.toJsonTree(directorList));
        responseObject.add("keywordList", gson.toJsonTree(keywordList));
        
        responseObject.addProperty("status", true);
        
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));
        
    }
    
}
