package hibernate;

public class CartItemDTO {
    private int id;
    private String movieTitle;
    private int movieId;
    private String cinemaName;
    private String hallName;
    private String showDate;
    private String showTime;
    private int quantity;
    private double unitPrice;
    private double subtotal;
    private final String posterPath;

    public CartItemDTO(CartItem item) {
        this.id = item.getId();
        this.movieId = item.getShowtime().getMovie().getId();
        this.movieTitle = item.getShowtime().getMovie().getTitle();
        this.posterPath = "movie-posters/" + item.getShowtime().getMovie().getId() + "/poster.png";
        this.cinemaName = item.getShowtime().getHall().getCinema().getName();
        this.hallName = item.getShowtime().getHall().getName();
        this.showDate = item.getShowtime().getShowDate().toString();
        this.showTime = item.getShowtime().getShowTime().toString();
        this.quantity = item.getQuantity();
        this.unitPrice = item.getUnit_price();
        this.subtotal = item.getSubtotal();
    }
}


