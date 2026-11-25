package model;

import hibernate.Booking;
import hibernate.BookingItems;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import hibernate.Showtime;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class GenerateTicketPDF {

    public static File generate(Booking booking, List<BookingItems> items) throws Exception {
        String filePath = System.getProperty("java.io.tmpdir") + "/ticket_" + booking.getId() + ".pdf";

        Document document = new Document(PageSize.A5); 
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.RED);
        Font normalFont = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.BLACK);
        Font labelFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.DARK_GRAY);

        Paragraph title = new Paragraph(" BlockBuster Movie Ticket ", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" ")); 

        // Booking details table
        PdfPTable bookingTable = new PdfPTable(2);
        bookingTable.setWidthPercentage(100);
        bookingTable.setSpacingBefore(10f);
        bookingTable.setSpacingAfter(10f);

        bookingTable.addCell(getCell("Booking ID:", labelFont));
        bookingTable.addCell(getCell("#" + booking.getId(), normalFont));

        bookingTable.addCell(getCell("Total Amount:", labelFont));
        bookingTable.addCell(getCell("LKR " + String.format("%.2f", booking.getTotal_amount()), normalFont));

        bookingTable.addCell(getCell("Date:", labelFont));
        bookingTable.addCell(getCell(booking.getCreated_at().toString(), normalFont));

        document.add(bookingTable);

        Paragraph ticketsHeader = new Paragraph("Tickets", labelFont);
        ticketsHeader.setAlignment(Element.ALIGN_CENTER);
        document.add(ticketsHeader);
        document.add(new Paragraph(" "));

        PdfPTable ticketTable = new PdfPTable(4); // Movie | Hall | Date & Time | Qty
        ticketTable.setWidthPercentage(100);
        ticketTable.setWidths(new float[]{3, 2, 4, 1});

        ticketTable.addCell(getHeaderCell("Movie"));
        ticketTable.addCell(getHeaderCell("Hall"));
        ticketTable.addCell(getHeaderCell("Date & Time"));
        ticketTable.addCell(getHeaderCell("Qty"));

        for (BookingItems item : items) {
            Showtime showtime = item.getShowtime();
            ticketTable.addCell(getCell(showtime.getMovie().getTitle(), normalFont));
            ticketTable.addCell(getCell(showtime.getHall().getName(), normalFont));
            ticketTable.addCell(getCell(showtime.getShowDate() + " " + showtime.getShowTime(), normalFont));
            ticketTable.addCell(getCell(String.valueOf(item.getQuantity()), normalFont));
        }

        document.add(ticketTable);

        // Footer
        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph("Enjoy your movie!", labelFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return new File(filePath);
    }

    private static PdfPCell getCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private static PdfPCell getHeaderCell(String text) {
        Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setBackgroundColor(Color.BLACK);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }
}