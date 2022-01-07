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
import java.sql.*;

@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/addMovie")
public class AddMovieServlet extends HttpServlet {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            JsonObject responseJsonObject = new JsonObject();

            String sql = "CALL updateNextId()";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rslt = stmt.executeQuery();
            stmt.close();
            rslt.close();

            String title = request.getParameter("title");
            String year = request.getParameter("year");
            String director = request.getParameter("director");
            String star = request.getParameter("star");
            String genre = request.getParameter("genre");

            /* This example only allows username/password to be test/test
            /  in the real project, you should talk to the database to verify username/password
            */
            String query = "CALL add_movie(?,?,?,?,?)";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1,title);
            statement.setInt(2,Integer.parseInt(year));
            statement.setString(3,director);
            statement.setString(4,star);
            statement.setString(5,genre);

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                String isAddMovie = rs.getString("isAddMovie");
                String isAddStar = rs.getString("isAddStar");
                String isAddGenre = rs.getString("isAddGenre");
                String message = "";
                if(isAddMovie.equals("true")) {
                    if(isAddStar.equals("true"))
                        message += " New star has been added. ";
                    else
                        message += " Star has already existed. ";

                    if(isAddGenre.equals("true"))
                       message += "New Genre has been added";
                    else
                        message += "Genre has already existed";

                    responseJsonObject.addProperty("message", "New movie has been added. " + message);
                }
                else
                    responseJsonObject.addProperty("message", "Duplicate movie, no movie, star or genre is added");

            }
            else
                responseJsonObject.addProperty("message", "Cannot add movie");

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
