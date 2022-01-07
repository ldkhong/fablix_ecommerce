import java.util.ArrayList;

/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final String username;
    private final String userId;
    private final boolean isAdmin;

    public User(String username,String userId,boolean isAdmin) {
        this.username = username;
        this.userId = userId;
        this.isAdmin = isAdmin;
    }

    public String getUsername() {
        return username;
    }

    public String getUserId() {
        return userId;
    }

    public boolean getIsAdmin() {return isAdmin;}

}
