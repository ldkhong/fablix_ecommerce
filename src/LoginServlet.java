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
import java.sql.*;

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    public String getServletInfo() {
        return "Servlet connects to MySQL database and displays result of a SELECT";
    }
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
        JsonObject responseJsonObject = new JsonObject();

        String device = request.getParameter("device");

        if (device == null) {
            String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
            // Verify reCAPTCHA
            try {
                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            } catch (Exception e) {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Please check on reCAPTCHA to login");
                response.getWriter().write(responseJsonObject.toString());
                return;
            }
        }

        String isLogin = request.getParameter("isLogin");
        HttpSession session = request.getSession();
        String previousURL = (String) session.getAttribute("previousURL");

        if(previousURL == null)
            previousURL = "movies.html";

        System.out.println(isLogin);
        if(isLogin.equals("false")){
            responseJsonObject.addProperty("status", "success");
            responseJsonObject.addProperty("previousURL", previousURL);
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        String username = request.getParameter("username").trim(); // customer email
        String password = request.getParameter("password").trim();

        if(username.equals("") || password.equals("")) {
            responseJsonObject.addProperty("status", "fail");
            if(username.equals("")) {
                responseJsonObject.addProperty("username", "true");
            }
            if(password.equals(""))
                responseJsonObject.addProperty("password", "true");
            response.getWriter().write(responseJsonObject.toString());
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            String query = "";
            boolean isAdmin = (request.getParameter("admin") != null)? true : false;

            if(isAdmin)
                query = "SELECT email as id, password from employees as e where e.email = ?";
            else
                query = "SELECT id, password from customers as c where c.email = ?";

            PreparedStatement statement = conn.prepareStatement(query);

            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String encryptedPassword = rs.getString("password");
                boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);

                if (success) {
                    request.getSession().setAttribute("user", new User(username, rs.getString("id"),isAdmin));
                    responseJsonObject.addProperty("status", "success");
                    String checkout = (String) session.getAttribute("checkout");
                    if(checkout != null) {
                        responseJsonObject.addProperty("previousURL", "checkout.html");
                    }
                    else
                        responseJsonObject.addProperty("previousURL", previousURL);
                }
                else {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "incorrect password");
                }
            }
            else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Username " + username + " doesn't exist");
            }
            rs.close();
            statement.close();

            response.getWriter().write(responseJsonObject.toString());
            response.setStatus(200);
        }
        catch (SQLException e){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());
            e.printStackTrace();
            response.setStatus(500);
        }

    }

}


