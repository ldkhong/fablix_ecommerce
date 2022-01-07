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
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
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
            String query ="Select id, title, year, director, rating, stars, group_concat(concat(gid,': ', genre) ORDER BY genre) as genres " +
                           // get genreName and genreId from genres table (I)
                          "From (Select id, title, year, director, rating, stars, genreId as gId, (Select name from genres where gId = id) as genre" +
                                // concat starId and starName orderBy number of movies played, break by starName to 1 row (II)
                               " From (Select id, title, year, director, rating, group_concat(concat(sId,\": \",starName) ORDER BY numberOfMovies DESC, starName ASC) as stars" +
                                       // find starIds, starNames and numbers of movies played by each star (III)
                                      " From (Select id, title, year, director, rating, starId as sId," +
                                            // get number of movies played by the stars
                                             "(Select count(sId) from stars_in_movies where sId = starId group by sId) as numberOfMovies," +
                                            // get names of star who played in the movie
                                             "(Select name from stars where sId = id) as starName" +
                                            // add rating column to the table
                                             " From (Select id, title, year, director, rating" +
                                                    // get id, title, year and director of movies that match the Id
                                                   " From (Select id, title, year, director from movies Where id = ? ) as reduce_movies " +
                                                    "Left Join ratings ON id = movieId) as reduce " +
                                             "Left Join stars_in_movies ON reduce.id = stars_in_movies.movieId) as reduce_table " +
                                     "GROUP BY id, title, year, director,rating) as movies_stars " +
                              "Left Join genres_in_movies ON movies_stars.id = genres_in_movies.movieId ) as movies_genres " +
                         "GROUP BY id, title, year, director, rating, stars ";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1,id);
            ResultSet rs = statement.executeQuery();

            HttpSession session = request.getSession(true);
            String previousURL = (String) session.getAttribute("previousURL");
            if(previousURL == null)
                previousURL = "movies.html";

            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("previousURL", previousURL);

            // Iterate through each row of rs
            while (rs.next()) {
                // Create a JsonObject based on the data we retrieve from rs
                responseJsonObject.addProperty("rating", rs.getString("rating"));
                responseJsonObject.addProperty("stars", rs.getString("stars"));
                responseJsonObject.addProperty("genres",  rs.getString("genres"));
                responseJsonObject.addProperty("id", rs.getString("id"));
                responseJsonObject.addProperty("title", rs.getString("title"));
                responseJsonObject.addProperty("year", rs.getString("year"));
                responseJsonObject.addProperty("director", rs.getString("director"));
            }
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
