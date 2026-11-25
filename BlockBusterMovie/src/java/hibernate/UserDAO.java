package hibernate;

import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class UserDAO {

    private static SessionFactory factory = new Configuration().configure().buildSessionFactory();

    public static boolean isEmailRegistered(String email) {
        Session session = factory.openSession();
        try {
            String hql = "FROM User WHERE email = :email";
            Query query = session.createQuery(hql);  
            query.setParameter("email", email);
            List<User> users = query.list();

            return !users.isEmpty();

        } finally {
            session.close();
        }
    }
}
