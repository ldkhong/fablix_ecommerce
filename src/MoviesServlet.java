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
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.lang.String;

// Declaring a WebServlet called StarsServlet, which maps to url "/api/movies"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // this query will get Id, Title, Director, Rating, all stars who played in the movie,
            // all genres of movie of all movies that match the search fields
            String query;
                    //concat all genreId and genreName to 1 row
            query = "Select id, title, year, director, rating, stars, group_concat(concat(gid,': ', genre) ORDER BY genre) as genres " +
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
                                            // get id, title, year and director of movies that match the search fields
                                            " From (Select id, title, year, director from movies ";

            // this query will get the total number of movies that match the search fields
            String count = "SELECT count(*) as total FROM movies ";

            /** Retrieve search fields from url parameters */
            String title = request.getParameter("title"); // movie title
            String year = request.getParameter("year"); // release year of movie
            String director = request.getParameter("director"); // directorName
            String star = request.getParameter("star"); // starName
            String genre = request.getParameter("genre"); // genre
            String type = request.getParameter("type"); // fullText - search - browse

            String limit = request.getParameter("limit"); // number of movie per page
            if(limit == null) limit = "10";

            String page = request.getParameter("page"); // current page
            if(page == null) page = "1";

            String sortTitle = request.getParameter("sortTitle"); // ASC by default
            if(sortTitle == null) sortTitle = "asc";

            String sortRating = request.getParameter("sortRating"); //DESC by default
            if(sortRating == null) sortRating = "desc";

            String orderBy = request.getParameter("orderBy"); // 1=Title-Rating or 2=Rating-title
            if(orderBy == null) orderBy ="1";

            /** add search fields to query to find match movies */
            ArrayList<String> listOfParameters = new ArrayList<>(); // list of parameters
            String searchMovie = "";

            // Browse by genres
            if(genre != null){
                searchMovie += "WHERE movies.id IN (SELECT movieId from genres_in_movies " +
                        "WHERE genres_in_movies.genreId = ? )";
                listOfParameters.add(genre);
            }
            else {
                boolean found = false; // found is false start with "When" statement, else add "And" to query
                if(title != null) {
                   if(title.equals("*")){
                       searchMovie += "WHERE movies.title NOT REGEXP '^[a-zA-Z0-9]'";
                   }
                   else if(type != null && type.equals("fullText")){ // Search from search bar
                       searchMovie += "WHERE MATCH(movies.title) AGAINST ( "; // full-text search
                       String[] words = title.split("[ ]+"); // split input string by " "
                       for (int i = 0; i < words.length; i++) {
                           listOfParameters.add("+" + words[i] + "*");
                           searchMovie += "? ";
                       }
                       searchMovie += "IN BOOLEAN MODE) ";
                   }
                   else {
                       searchMovie += "WHERE movies.title LIKE ? "; // search title
                       listOfParameters.add(title + "%"); // add title to list of search elements
                       found = true;
                   }
                }
                // check other fields if query is from advance search
                if(type != null && type.equals("search")) {
                    if (year != null) {
                        searchMovie += (found) ? "AND" : "WHERE"; // check if Where is already in the query
                        searchMovie += " movies.year = ? ";  // add year to query
                        listOfParameters.add(year);
                        found = true;
                    }

                    if (director != null) {
                        searchMovie += (found) ? "AND" : "WHERE";
                        searchMovie += " movies.director LIKE ? ";
                        listOfParameters.add(director + "%");
                        found = true;
                    }
                    if (star != null) {
                        searchMovie += (found) ? "AND" : "WHERE";
                        searchMovie += " movies.id IN (SELECT movieId from stars_in_movies, stars " +
                                "WHERE stars.id = stars_in_movies.starId AND " +
                                "stars.name LIKE ? )";
                        listOfParameters.add("%" + star + "%");
                    }
                }
            }

            count += searchMovie; // end count query

            /**  continue for search query */
            searchMovie += ") as reduce_movies Left join ratings ON id = movieId ";

            // Sorting the filter movies tables, then only take the certain amount of movies to display on the page
            String sort;
            if (orderBy.equals("1"))
                sort = "ORDER BY rating " + sortRating +  ", title " + sortTitle; // sort by Rating, break by title
            else
                sort = "ORDER BY title " + sortTitle + ", rating " + sortRating; // sort by title, break by Rating

            int offset = (Integer.parseInt(page) - 1) * Integer.parseInt(limit);

            searchMovie += sort + " Limit " + limit + " OFFSET " + offset + " ) as reduce " +
                    //Left join with stars_in_movies table to get starName column  (map with (III) above)
                    "left join stars_in_movies ON reduce.id = stars_in_movies.movieId) as reduce_table " +
                    //concat starID and starName of the same movies to 1 row (II)
                    "GROUP BY id, title, year, director,rating) as movies_stars "+
                    //Left join with genres_in_movies to get genreName column (I)
                    "Left join genres_in_movies ON movies_stars.id = genres_in_movies.movieId ) as movies_genres "+
                    //concat genreId and GenreName to 1 row, and re-sort the table (group by ignore the sort result above)
                    "GROUP BY id, title, year, director, rating, stars " + sort;

            query += searchMovie; // end search query

            /** update previous URL and retrieve total Items ordered by customer in session */
            HttpSession session = request.getSession(true);
            String currentURL = "movies.html?"+request.getQueryString();
            session.setAttribute("previousURL", currentURL);

            Integer totalItems = (Integer) session.getAttribute("totalItems");
            if(totalItems == null) {
                totalItems = 0;
                session.setAttribute("totalItems", totalItems);
            }

            /** Prepare count and search Query with PreparedStatement to prevent sql injection attack */
            PreparedStatement countStatement = conn.prepareStatement(count);
            PreparedStatement searchStatement = conn.prepareStatement(query);
            for(int i = 0; i < listOfParameters.size();i++) {
                if(listOfParameters.get(i).matches("-?\\d+(\\.\\d+)?")) {
                    searchStatement.setInt(i + 1, Integer.parseInt(listOfParameters.get(i)));
                    countStatement.setInt(i + 1, Integer.parseInt(listOfParameters.get(i)));
                }
                else {
                    searchStatement.setString(i + 1, listOfParameters.get(i));
                    countStatement.setString(i + 1, listOfParameters.get(i));
                }
            }

            ResultSet searchResult = searchStatement.executeQuery(); // execute search query
            ResultSet countResult = countStatement.executeQuery(); // execute count query

            /** Create return jsonObject */
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("totalItems",totalItems); // number of order Movies
            responseJsonObject.addProperty("limit",limit); // number of movies display on a page
            responseJsonObject.addProperty("page",page); // current page number
            responseJsonObject.addProperty("sortTitle", sortTitle); // sort title by ASC or DESC
            responseJsonObject.addProperty("sortRating", sortRating); // sort Rating by ASC or DESC
            responseJsonObject.addProperty("orderBy", orderBy); // sort by rating and break by title or otherwise

            if(countResult.next()) // total movies from search result
                responseJsonObject.addProperty("total",countResult.getString("total"));

            // store list of movies from search result
            JsonArray jsonArray = new JsonArray();
            // Iterate through each row of searchResult
            while (searchResult.next()) {
                // Create a JsonObject based on the data we retrieve from searchResult
                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("id", searchResult.getString("id"));
                jsonObject.addProperty("title", searchResult.getString("title"));
                jsonObject.addProperty("year", searchResult.getString("year"));
                jsonObject.addProperty("director", searchResult.getString("director"));
                jsonObject.addProperty("rating", searchResult.getString("rating"));
                jsonObject.addProperty("stars", searchResult.getString("stars"));
                jsonObject.addProperty("genres", searchResult.getString("genres"));

                jsonArray.add(jsonObject);
            }
            responseJsonObject.add("movies", jsonArray);

            // close statement
            countResult.close();
            searchResult.close();
            searchStatement.close();
            countStatement.close();

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
