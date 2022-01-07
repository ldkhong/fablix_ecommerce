import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// server endpoint URL
@WebServlet("/movie-suggestion")
public class MovieSuggestion extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public MovieSuggestion() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection conn = dataSource.getConnection()){
            // setup the response json arrray
            JsonArray jsonArray = new JsonArray();

            // get the query string from parameter
            String query = request.getParameter("query");

            // return the empty json array if query is null or empty
            if (query == null || query.trim().isEmpty()) {
                response.getWriter().write(jsonArray.toString());
                return;
            }

            // Create mysql sql for full-text search using Prepared Statement
            String suggest = "SELECT id, title FROM movies where MATCH(title) AGAINST ( ";
            ArrayList<String> list_search = new ArrayList<>();

            String[] words = query.split("[ ]+");
            for(int i = 0; i < words.length; i++) {
                System.out.println(words[i]);
                list_search.add("+" +words[i] + "*");
                suggest += "? ";
            }
            suggest += "IN BOOLEAN MODE) LIMIT 10";

            PreparedStatement statement = conn.prepareStatement(suggest);
            for(int i = 0; i < list_search.size();i++)
                    statement.setString(i + 1, list_search.get(i));

            ResultSet rs = statement.executeQuery();


            while(rs.next()) {
                jsonArray.add(generateJsonObject(rs.getString("id"), rs.getString("title")));
            }
            statement.close();
            rs.close();
            response.getWriter().write(jsonArray.toString());
            return;
        } catch (Exception e) {
            System.out.println(e);
            response.sendError(500, e.getMessage());
        }
    }

    /*
     * Generate the JSON Object from hero to be like this format:
     * {
     *   "value": "Iron Man",
     *   "data": { "MovieID": 11 }
     * }
     *
     */
    private static JsonObject generateJsonObject(String movieID, String title) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", title);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movieID", movieID);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }


}
