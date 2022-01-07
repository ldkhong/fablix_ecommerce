import com.google.gson.JsonArray;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Construct a query with parameter represented by "?"
            String query = "SELECT * from stars as s, stars_in_movies as sim, movies as m " +
                    "where m.id = sim.movieId and sim.starId = s.id and s.id = ? ORDER BY m.year DESC, m.title ASC";

            PreparedStatement statement = conn.prepareStatement(query); // Declare our statement

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            // get previous URL
            HttpSession session = request.getSession(true);
            String previousURL = (String) session.getAttribute("previousURL");
            if(previousURL == null)
                previousURL = "movies.html";

            // return JSON object
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("previousURL", previousURL);

            // Iterate through each row of rs
            JsonArray jsonArray = new JsonArray();
            boolean found = false;
            while (rs.next()) {
                // Create a JsonObject based on the data we retrieve from rs
                if(!found) {
                    responseJsonObject.addProperty("starName", rs.getString("name"));
                    responseJsonObject.addProperty("starYear", rs.getString("birthYear"));
                    found = true;
                }

                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("movieId",  rs.getString("movieId"));
                jsonObject.addProperty("movieTitle", rs.getString("title"));
                jsonObject.addProperty("movieYear", rs.getString("year"));

                jsonArray.add(jsonObject);
            }

            responseJsonObject.add("movies", jsonArray);
            rs.close();
            statement.close();

            // write JSON string to output
            out.write(responseJsonObject.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // always remember to close db connection after usage. Here it's done by try-with-resources


    }

}
