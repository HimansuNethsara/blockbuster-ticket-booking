package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.BookingItems;
import hibernate.HibernateUtil;
import hibernate.Movie;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "AdminDashboardStat", urlPatterns = {"/AdminDashboardStat"})
public class AdminDashboardStat extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.openSession();

        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

            // total users
            Long totalUserCount = (Long) session.createQuery(
                "SELECT COUNT(u.id) FROM User u"
            ).uniqueResult();

            // New users today
            Long newUserCount = (Long) session.createQuery(
                "SELECT COUNT(u.id) FROM User u WHERE u.created_at >= :start AND u.created_at < :end"
            )
            .setParameter("start", java.sql.Timestamp.valueOf(startOfDay))
            .setParameter("end", java.sql.Timestamp.valueOf(endOfDay))
            .uniqueResult();

            // Total Revenue
            Criteria totalRevenueCriteria = session.createCriteria(BookingItems.class, "bi");
            totalRevenueCriteria.setProjection(Projections.sum("subtotal"));
            Number totalRevenueResult = (Number) totalRevenueCriteria.uniqueResult();
            Double totalRevenue = totalRevenueResult != null ? totalRevenueResult.doubleValue() : 0.0;
            
            // Daily Revenue
            Criteria dailyRevenueCriteria = session.createCriteria(BookingItems.class, "bi");
            dailyRevenueCriteria.createAlias("bi.booking", "b"); 
            dailyRevenueCriteria.add(Restrictions.ge("b.created_at", java.sql.Timestamp.valueOf(startOfDay)));
            dailyRevenueCriteria.add(Restrictions.lt("b.created_at", java.sql.Timestamp.valueOf(endOfDay)));
            dailyRevenueCriteria.setProjection(Projections.sum("subtotal"));
            Number dailyRevenueResult = (Number) dailyRevenueCriteria.uniqueResult();
            Double dailyRevenue = dailyRevenueResult != null ? dailyRevenueResult.doubleValue() : 0.0;
            
            // This Year's Releases
            Criteria thisYearMoviesCriteria = session.createCriteria(Movie.class);
            thisYearMoviesCriteria.add(Restrictions.eq("releaseYear", today.getYear()));
            thisYearMoviesCriteria.setProjection(Projections.rowCount());
            Long thisYearReleaseCount = (Long) thisYearMoviesCriteria.uniqueResult();

            // Total Movies
            Criteria totalMoviesCriteria = session.createCriteria(Movie.class);
            totalMoviesCriteria.setProjection(Projections.rowCount());
            Long totalMovieCount = (Long) totalMoviesCriteria.uniqueResult();
            
            // Top 3 Users by Booking Count
            Criteria bookingCriteria = session.createCriteria(BookingItems.class, "bi");
            bookingCriteria.createAlias("bi.booking", "b");
            bookingCriteria.createAlias("b.user", "u");

            bookingCriteria.setProjection(Projections.projectionList()
                .add(Projections.groupProperty("u.id"))          // row[0]
                .add(Projections.property("u.first_name"))       // row[1]
                .add(Projections.property("u.last_name"))        // row[2]
                .add(Projections.property("u.email"))            // row[3]
                .add(Projections.sum("bi.quantity"), "ticketsCount")// row[4]
            );

            bookingCriteria.addOrder(Order.desc("ticketsCount"));
            bookingCriteria.setMaxResults(3);

            List<Object[]> topUsers = bookingCriteria.list();

            JsonObject json = new JsonObject();
            json.addProperty("totalUserCount", totalUserCount);
            json.addProperty("newUserCount", newUserCount);
            json.addProperty("totalRevenue", totalRevenue);
            json.addProperty("dailyRevenue", dailyRevenue);
            json.addProperty("thisYearReleases", thisYearReleaseCount != null ? thisYearReleaseCount : 0);
            json.addProperty("totalMovies", totalMovieCount != null ? totalMovieCount : 0);

            JsonArray topUsersArray = new JsonArray();
            for (Object[] row : topUsers) {
                JsonObject userJson = new JsonObject();
                userJson.addProperty("first_name", String.valueOf(row[1]));
                userJson.addProperty("last_name", String.valueOf(row[2]));
                userJson.addProperty("email", String.valueOf(row[3]));

                 Number ticketsCount = (Number) row[4];
                userJson.addProperty("bookingCount", ticketsCount != null ? ticketsCount.longValue() : 0);

                topUsersArray.add(userJson);
            }

            json.add("topUsers", topUsersArray);
            
            // Total ticket bookings (sum of all quantities)
            Long totalTickets = (Long) session.createQuery(
                "SELECT SUM(bi.quantity) FROM BookingItems bi"
            ).uniqueResult();

            // New ticket bookings today
            Long newTickets = (Long) session.createQuery(
                "SELECT SUM(bi.quantity) FROM BookingItems bi WHERE bi.booking.created_at >= :start AND bi.booking.created_at < :end"
            )
            .setParameter("start", java.sql.Timestamp.valueOf(startOfDay))
            .setParameter("end", java.sql.Timestamp.valueOf(endOfDay))
            .uniqueResult();
            
            json.addProperty("totalTicketCount", totalTickets != null ? totalTickets : 0);
            json.addProperty("newTicketCount", newTickets != null ? newTickets : 0);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json.toString());

        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            session.close();
        }
    }
    
}
