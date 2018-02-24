package in.gitbox.cryptonian;

/**
 * Created by sagar on 23-02-2018.
 */

public class Products {

    private String productName;
    private String productImages;
    private String productTitle;
    private String productDescription;
    private String paymentMethod;
    private String timeStamp;
    private Users users;

    public Products(String productName, String productImages, String productTitle, String productDescription, String paymentMethod, String timeStamp, Users users) {
        this.productName = productName;
        this.productImages = productImages;
        this.productTitle = productTitle;
        this.productDescription = productDescription;
        this.paymentMethod = paymentMethod;
        this.timeStamp = timeStamp;
        this.users = users;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductImages() {
        return productImages;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public Users getUsers() {
        return users;
    }
}
