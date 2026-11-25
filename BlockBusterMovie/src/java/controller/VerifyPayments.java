
package controller;

import hibernate.Booking;
import hibernate.BookingItems;
import hibernate.HibernateUtil;
import hibernate.Showtime;
import hibernate.User;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.GenerateTicketPDF;
import model.Mail;
import model.PayHere;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "VerifyPayments", urlPatterns = {"/VerifyPayments"})
public class VerifyPayments extends HttpServlet {

    private static final int PAYMENT_SUCCESS = 1; 

    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String merchant_id = request.getParameter("merchant_id");
        String order_id = request.getParameter("order_id");
        String payhere_amount = request.getParameter("payhere_amount");
        String payhere_currency = request.getParameter("payhere_currency");
        String status_code = request.getParameter("status_code");
        String md5sig = request.getParameter("md5sig");

        String merchantSecret = "PAYHERE_MERCHANT_SECRET";
        String merchantSecretMD5 = PayHere.generateMd5(merchantSecret);
        
        double amountValue = Double.parseDouble(payhere_amount);
        String formattedAmount = String.format("%.2f", amountValue);

        String hash = PayHere.generateMd5(merchant_id + order_id + formattedAmount + payhere_currency + status_code + merchantSecretMD5);

        if (md5sig.equals(hash) && Integer.parseInt(status_code) == 2) {   //success
            System.out.println("Payment verified successfully for order: " + order_id);
            
            String bookingIdStr = order_id.replace("#BOOK", "");
            int bookingId = Integer.parseInt(bookingIdStr);

            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = session.beginTransaction();

            Booking booking = (Booking) session.get(Booking.class, bookingId);

            if (booking != null) {
                User user = booking.getUser();

                Criteria c = session.createCriteria(BookingItems.class);
                c.add(Restrictions.eq("booking", booking));
                List<BookingItems> items = c.list();

                String emailHtml = getSimpleThankYouEmailTemplate(user.getFirst_name(), booking.getId(), booking.getTotal_amount());

                // Send Email in a new thread
                new Thread(() -> {
                        try {
                            File pdfFile = GenerateTicketPDF.generate(booking, items);
                            System.out.println("Sending ticket email with PDF to: " + user.getEmail());
                            Mail.sendMailWithAttachment(user.getEmail(), "Your BlockBuster Movie Ticket", emailHtml, pdfFile);
                        } catch (Exception e) {
                        }
                }).start();

                tx.commit();
            }
            
            session.close();
        }
    }
    
    private String getSimpleThankYouEmailTemplate(String name, int bookingId, double totalAmount) {
        return "<!DOCTYPE html>" +
           "<html><head><meta charset='UTF-8'><style>" +
           "body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #0d0d0d; margin: 0; padding: 0; color: #fff; }" +
           ".container { background: linear-gradient(145deg, #1c1c1c, #0d0d0d); margin: 20px auto; padding: 30px; border-radius: 10px; max-width: 600px; " +
           "box-shadow: 0 4px 20px rgba(0,0,0,0.6); border: 2px solid #dd003f; }" +
           "h1 { color: #dd003f; text-align: center; font-size: 28px; margin-bottom: 10px; }" +
           "h2 { color: #ffcc00; text-align: center; font-size: 28px; margin-bottom: 10px; }" +
           "p { font-size: 16px; line-height: 1.5; text-align: center; color: #fff; }" +
           ".ticket { background: #111; padding: 15px; margin-top: 20px; border-radius: 8px; border: 1px dashed #ffcc00; text-align: center; }" +
           ".ticket strong { color: #ffcc00; font-size: 18px; }" +
           ".footer { text-align: center; margin-top: 20px; font-size: 13px; color: #aaa; }" +
           ".movie-icon { display: block; margin: 0 auto 15px auto; width: 80px; }" +
           "</style></head><body>" +
           "<div class='container'>" +
           "<h1> BlockBuster!</h1>" +
           "<h2> Thank You, " + name + "!</h2>" +
           "<p>Your movie booking has been <strong style='color:#4CAF50;'>CONFIRMED</strong>.</p>" +
           "<div class='ticket'>" +
           "<p>Booking ID: <strong>#" + bookingId + "</strong></p>" +
           "<p>Total Paid: <strong>LKR " + String.format("%.2f", totalAmount) + "</strong></p>" +
           "</div>" +
           "<p>We've attached your <strong>movie ticket PDF</strong> to this email.<br>Don't forget the popcorn! </p>" +
           "<div class='footer'>Enjoy your movie with <span style='color:#dd003f;'>BlockBuster</span> — Where Every Seat Feels Like the Front Row.</div>" +
           "</div></body></html>";
    }

}

