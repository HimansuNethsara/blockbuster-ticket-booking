package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.Cinema;
import hibernate.Hall;
import hibernate.HibernateUtil;
import hibernate.Movie;
import hibernate.Showtime;
import hibernate.Ticket;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

@WebServlet("/LoadTicketData")
public class LoadTicketData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject json = new JsonObject();
        json.addProperty("status", false);

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            String action = request.getParameter("action");

            if (action == null) {
                json.addProperty("message", "Action parameter is required");
            } else if ("cinemas".equals(action)) {
                Criteria cinemaCriteria = session.createCriteria(Cinema.class);
                List<Cinema> cinemas = cinemaCriteria.list();

                JsonArray array = new JsonArray();
                for (Cinema c : cinemas) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", c.getId());
                    obj.addProperty("name", c.getName());
                    array.add(obj);
                }
                json.add("cinemas", array);
                json.addProperty("status", true);

            } else if ("halls".equals(action)) {
                String cinemaIdStr = request.getParameter("cinemaId");
                if (cinemaIdStr == null) {
                    json.addProperty("message", "cinemaId parameter is required");
                } else {
                    try {
                        int cinemaId = Integer.parseInt(cinemaIdStr);
                        Cinema cinema = (Cinema) session.get(Cinema.class, cinemaId);

                        if (cinema == null) {
                            json.addProperty("message", "Cinema not found");
                        } else {
                            Criteria hallCriteria = session.createCriteria(Hall.class);
                            List<Hall> halls = hallCriteria.list();

                            JsonArray array = new JsonArray();
                            for (Hall h : halls) {
                                if (h.getCinema().getId() == cinemaId) {
                                    JsonObject obj = new JsonObject();
                                    obj.addProperty("id", h.getId());
                                    obj.addProperty("name", h.getName());
                                    array.add(obj);
                                }
                            }
                            json.add("halls", array);
                            json.addProperty("status", true);
                        }
                    } catch (NumberFormatException e) {
                        json.addProperty("message", "Invalid cinemaId format");
                    }
                }

            } else if ("showtimes".equals(action)) {
                String movieIdStr = request.getParameter("movieId");
                String cinemaIdStr = request.getParameter("cinemaId");
                String hallIdStr = request.getParameter("hallId");
                String dateStr = request.getParameter("date");

                if (movieIdStr == null || cinemaIdStr == null || hallIdStr == null || dateStr == null) {
                    json.addProperty("message", "Missing parameters for showtimes");
                } else {
                    try {
                        int movieId = Integer.parseInt(movieIdStr);
                        int cinemaId = Integer.parseInt(cinemaIdStr);
                        int hallId = Integer.parseInt(hallIdStr);
                        java.sql.Date showDate = java.sql.Date.valueOf(dateStr);

                        Movie movie = (Movie) session.get(Movie.class, movieId);
                        Cinema cinema = (Cinema) session.get(Cinema.class, cinemaId);
                        Hall hall = (Hall) session.get(Hall.class, hallId);

                        if (movie == null) {
                            json.addProperty("message", "Invalid movie ID");
                        } else if (cinema == null) {
                            json.addProperty("message", "Invalid cinema ID");
                        } else if (hall == null || hall.getCinema().getId() != cinemaId) {
                            json.addProperty("message", "Invalid hall ID or hall does not belong to selected cinema");
                        } else {
                            Criteria showtimeCriteria = session.createCriteria(Showtime.class);
                            List<Showtime> allShowtimes = showtimeCriteria.list();

                            JsonArray array = new JsonArray();
                            for (Showtime st : allShowtimes) {
                                if (st.getMovie().getId() == movieId &&
                                    st.getHall().getId() == hallId &&
                                    st.getHall().getCinema().getId() == cinemaId &&
                                    st.getShowDate().equals(showDate)) {

                                    JsonObject obj = new JsonObject();
                                    obj.addProperty("id", st.getId());
                                    obj.addProperty("showTime", st.getShowTime().toString());
                                    array.add(obj);
                                }
                            }
                            json.add("showtimes", array);
                            json.addProperty("status", true);
                        }

                    } catch (NumberFormatException | DateTimeParseException e) {
                        json.addProperty("message", "Invalid parameter format");
                    }
                }

            } else if ("availability".equals(action)) {
                String showtimeIdStr = request.getParameter("showtimeId");
                if (showtimeIdStr == null) {
                    json.addProperty("message", "showtimeId parameter is required");
                } else {
                    try {
                        int showtimeId = Integer.parseInt(showtimeIdStr);
                        Showtime st = (Showtime) session.get(Showtime.class, showtimeId);

                        if (st == null) {
                            json.addProperty("message", "Showtime not found");
                        } else {
                            int totalSeats = st.getHall().getTotalSeats();

                            Criteria ticketCriteria = session.createCriteria(Ticket.class);
                            ticketCriteria.add(Restrictions.eq("showtime.id", showtimeId));
                            Ticket ticket = (Ticket) ticketCriteria.uniqueResult();

                            int soldCount = (ticket != null) ? ticket.getSoldCount() : 0;
                            int available = totalSeats - soldCount;

                            json.addProperty("price", st.getPrice());
                            json.addProperty("available", available);
                            json.addProperty("status", true);
                        }
                    } catch (NumberFormatException e) {
                        json.addProperty("message", "Invalid showtimeId format");
                    }
                }

            } else {
                json.addProperty("message", "Unknown action: " + action);
            }

        } catch (Exception e) {
            e.printStackTrace();
            json.addProperty("message", "Server error: " + e.getMessage());
        } finally {
            if (session != null) session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(json));
    }
}

