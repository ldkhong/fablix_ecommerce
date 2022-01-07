import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/Cart.
 */
@WebServlet(name = "ConfirmServlet", urlPatterns = "/api/confirm")
public class ConfirmServlet extends HttpServlet {

    /**
     * handles GET requests to store session information
     */

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        HttpSession session = request.getSession();

        JsonObject responseJsonObject = new JsonObject();

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

        String saleId = (String) session.getAttribute("lastId");
        responseJsonObject.addProperty("lastId", saleId);

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());

        //clear session
        saleId = "";
        cartItems.clear();

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());

    }

}
