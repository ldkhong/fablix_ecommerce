import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "BrowseServlet", urlPatterns = "/api/browse")
public class BrowseServlet extends HttpServlet {
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
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            Statement statement = conn.createStatement();
            //Query to retrieve genreId and genreName from genres tables
            String query = "SELECT * FROM genres";

            //Execute the query and save the data in rs
            ResultSet rs = statement.executeQuery(query);

            //Create JsonArray which each of the element is jsonObject
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                String genre_id = rs.getString("id");
                String genre_name = rs.getString("name");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("genre_id", genre_id);
                jsonObject.addProperty("genre_name", genre_name);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            out.write(jsonArray.toString());
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
    }
}
