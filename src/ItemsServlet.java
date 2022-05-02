import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

// Declaring a WebServlet called ItemServlet, which maps to url "/items"
@WebServlet(name = "ItemServlet", urlPatterns = "/api/items")
public class ItemsServlet extends HttpServlet {

    // handle request to edit or add items
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type
        HttpSession session = request.getSession(true);

        PrintWriter out = response.getWriter();

        String type = request.getParameter("type"); // type can be edit or add

        String title = request.getParameter("title");
        Integer qty =  Integer.parseInt(request.getParameter("qty"));
        String id = request.getParameter("id");

        // Key = movie id, Value is a pair of {movie title and quantity}
        HashMap<String, Pair > cartItems = (HashMap<String, Pair>) session.getAttribute("cartItems");
        if(cartItems == null) {
            cartItems = new HashMap<String, Pair>();
            session.setAttribute("cartItems", cartItems);
        }

        //total number of items
        Integer totalItems = (Integer) session.getAttribute("totalItems");
        if(totalItems == null) totalItems = 0;

        // edit quantity of item
        if(type.equals("edit")) { // "?type=edit"+"&qty="+id+"&id="+i
            totalItems += qty - cartItems.get(id).getQty();

            synchronized (cartItems) {
                if (qty == 0)
                    cartItems.remove(id);
                else {
                    Pair item = cartItems.get(id);
                    item.setQty(qty);
                    cartItems.put(id, item);
                }
            }
        }

        // add item to cart
        else {
            JsonObject responseJsonObject = new JsonObject();
            if (qty > 0) {
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message",  qty + " item(s) added");

                // lock cartItems
                synchronized (cartItems) {
                    int currentQty = 0;
                    if(cartItems.containsKey(id))
                        currentQty = cartItems.get(id).getQty();
                    cartItems.put(id, new Pair (title, currentQty + qty));
                }
                totalItems += qty;
            }

            else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "No item added");
            }
            responseJsonObject.addProperty("totalItems", totalItems);
            responseJsonObject.addProperty("title", title);
            response.getWriter().write(responseJsonObject.toString());
        }
        session.setAttribute("totalItems", totalItems);
    }
}
