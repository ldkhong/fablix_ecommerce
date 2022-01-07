import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "CheckoutServlet", urlPatterns = "/api/checkout")
public class CheckoutServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            HttpSession session = request.getSession();

            JsonObject responseJsonObject = new JsonObject();

            String lastname = request.getParameter("lastname");
            String firstname = request.getParameter("firstname");
            String exp = request.getParameter("exp");
            String cardId = request.getParameter("card_id");

            //check if card is valid or not.
            String query = "SELECT * from creditcards where id = ? AND lastName = ? AND firstName = ? AND expiration = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1,cardId);
            statement.setString(2,lastname);
            statement.setString(3, firstname);
            statement.setString(4,exp);

            ResultSet rs = statement.executeQuery();

            // Order is placed successfully
            if (rs.next()) {
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "Transaction success");

                HashMap<String, Pair > cartItems = (HashMap<String, Pair>) session.getAttribute("cartItems");

                User customer = (User) session.getAttribute("user");

               // System.out.println(customer.getUserId());

                String customerId = customer.getUserId();

                // get the date when user purchases
                Timestamp timestamp = new Timestamp(new Date().getTime());
                session.setAttribute("time",timestamp);

                //find the lastID sale order in the data - to generate the order ID in confirm page
                Statement statement3 = conn.createStatement();
                String query3 = "Select id from sales ORDER BY id DESC LIMIT 1";
                ResultSet rs3 = statement3.executeQuery(query3);

                if (rs3.next()) {
                    String lastId = rs3.getString("id");
                    session.setAttribute("lastId", lastId);
                }

                statement3.close();
                rs3.close();

                // Insert to sales table customer order (sales table does not have quantity column)
                for(Map.Entry<String, Pair> item : cartItems.entrySet()) {
                    String id = item.getKey();
                    int qty = item.getValue().getQty();

                    for(int i = 0; i < qty; i++) {
                        Statement statement2 = conn.createStatement();
                        String insertSale = "INSERT INTO sales (customerId,movieId,saleDate) VALUES ('" + customerId +"','"
                                + id + "','"+timestamp+"')";
                        statement2.executeUpdate(insertSale);
                        statement2.close();
                    }
                }
            }

            // invalid payment card
            else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "invalid payment card");
            }

            rs.close();
            statement.close();

            response.getWriter().write(responseJsonObject.toString());
            response.setStatus(200);
        }
        catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

    }
}
