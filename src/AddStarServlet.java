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


@WebServlet(name = "AddStarServlet", urlPatterns = "/api/addStar")
public class AddStarServlet extends HttpServlet {
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

            String star = request.getParameter("star");
            String birthYear = request.getParameter("year");
            if(birthYear == null)
                birthYear = "0";

            System.out.println(star + " " + birthYear);

            String sql = "CALL updateNextId()";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rslt = stmt.executeQuery();
            stmt.close();
            rslt.close();

            String query = "CALL add_star(?,?,1)"; //1 mean get message if star is added or not

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1,star);
            statement.setInt(2,Integer.parseInt(birthYear));

            ResultSet rs = statement.executeQuery();

            if(rs.next()){
                responseJsonObject.addProperty("message", "New star has been added");
            }
            else{
                responseJsonObject.addProperty("message", "Fail to add new star");
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
