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
import java.sql.*;


@WebServlet(name = "MetadataServlet", urlPatterns = "/api/metadata")
public class MetadataServlet extends HttpServlet {
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

            String query = "Show tables"; //1 mean get message if star is added or not

            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            JsonArray jsonArray = new JsonArray();
            while(rs.next()){
                String table = rs.getString(1);
                System.out.println(table);
                if(!table.equals("nextId")) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("table", table);

                    Statement stmt = conn.createStatement();
                    String sql = "describe " + table;
                    ResultSet rs2 = stmt.executeQuery(sql);
                    JsonArray rowTb = new JsonArray();
                    while(rs2.next()) {
                        JsonObject jO = new JsonObject();
                        jO.addProperty("field", rs2.getString(1));
                        jO.addProperty("type", rs2.getString(2));
                        jO.addProperty("null", rs2.getString(3));
                        jO.addProperty("key", rs2.getString(4));
                        jO.addProperty("default", rs2.getString(5));
                        jO.addProperty("extra", rs2.getString(6));
                        rowTb.add(jO);
                    }
                    stmt.close();
                    rs2.close();
                    jsonObject.add("body",rowTb);
                    jsonArray.add(jsonObject);
                }
            }

            rs.close();
            statement.close();

            response.getWriter().write(jsonArray.toString());
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

