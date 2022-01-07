import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/Cart.
 */
@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {

    /**
     * handles POST requests to show the item list information
     */

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        // get the link to previous page for continue shopping button
        String previousURL = (String) session.getAttribute("previousUrl");
        if(previousURL == null)
            previousURL = "movies.html";

        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("previousUrl", previousURL);

        // get cartItems session which contains the list of ordered items
        HashMap<String, Pair > cartItems = (HashMap<String, Pair>) session.getAttribute("cartItems");
        if(cartItems == null)
            cartItems = new HashMap<String, Pair> ();

        JsonArray items = new JsonArray();

        for(Map.Entry<String, Pair> item : cartItems.entrySet()) {
            String id = item.getKey();
            Pair title_qty = item.getValue();

            JsonObject oneItem = new JsonObject();
            oneItem.addProperty("id", id);
            oneItem.addProperty("title", title_qty.getTitle());
            oneItem.addProperty("quantity", title_qty.getQty());

            items.add(oneItem);
        }

        responseJsonObject.add("items", items);

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }
}
