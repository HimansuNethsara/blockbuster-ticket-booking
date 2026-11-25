package controller;

import hibernate.Genre;
import hibernate.HibernateUtil;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 *
 * @author Dracula
 */
@WebServlet(name = "A", urlPatterns = {"/A"})
public class A extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String name = request.getParameter("name");
        
        Genre genre = new Genre();
        
        Session s = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        
        try{
        
            tx = s.beginTransaction();
            s.save(genre);
            tx.commit();
            s.beginTransaction().commit();
            
        }catch(Exception e){
            if (tx!=null) {
                tx.rollback();
            }
            
        }
        
        response.setContentType("application/json");
        
    }

}
