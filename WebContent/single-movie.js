/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResult(resultData) {
    let movieHTML = "<div class=\"single-title\">" + resultData["title"] +" (" +resultData["year"] +")</div>\n" +
                    "<div class=\"single-line\">Director: " + resultData["director"] + "</div>" ;

    if(resultData["rating"] == null)
        movieHTML += "<div class=\"single-line\">Rating: N/A</div>";
    else
        movieHTML += "<div class=\"single-line\">Rating: " + resultData["rating"] + "</div>";

    let genres = resultData["genres"];
    movieHTML += "<div class=\"single-genre\">";
    if(genres != null) {
        movieHTML += "<span>Genres: </span>";
        movieHTML += "<ul>";
        let genre_id_name = genres.split(',');

        for (let j = 0; j < genre_id_name.length; j++) {
            let id_genre = genre_id_name[j].split(': ');
            movieHTML += '<li><a href="movies.html?genre=' + id_genre[0] +'">' + id_genre[1] +'</a></li>';
        }
        movieHTML += "</ul>";
    }
    else
        movieHTML += "<span>Genres: No Information</span>";
    movieHTML += "</div>";

    movieHTML += "<div class=\"single-list\">";
    let stars = resultData["stars"];
    if(stars != null) {
        movieHTML += "<span>Starring by: <span>";
        movieHTML += "<ul>";

        let starsArray = stars.split(',');
        for (let j = 0; j < starsArray.length; j++) {
            let id_name = starsArray[j].split(': ');
            movieHTML += '<li><a href="single-star.html?id=' + id_name[0] + '">'
                + id_name[1] +'</a></li>';
        }
        movieHTML += "</ul>";
    }
    else
        movieHTML += "<span>Starring by: No Information<span>";
    movieHTML += '<div class="other-btn" style="margin-top: 5px;">\n' +
        '    <a href="#" class="btn btn-outline-primary" id="go-back-btn" style="margin-left: 10px">Go Back</a>\n' +
        '    <button class="btn btn-outline-primary" id="btn-add">Add to Cart</button>\n' +
        '</div>'
    movieHTML += "</div>";

    $("#single-movie").append(movieHTML);
    $("#go-back-btn").attr("href", resultData["previousURL"]);
    $("#btn-add").on('click', function () {addItems(resultData["id"], resultData["title"])});
}

function handleAddResult(resultDataJson) {
    // display a success message if
    if (resultDataJson["status"] === "success") {
        $("#success_message").empty().append(
            '<p class="fade-message">' + resultDataJson["title"] + " has been added to cart!" + '</p>'
        );

        // display an error message if adding quantity is 0
    } else {
        $("#error_message").text(resultDataJson["message"]);
    }
    let total_items = $("#total-items");
    total_items.css("display", "block");
    total_items.text(resultDataJson["totalItems"]);
}

function addItems(id, title) {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/items?type=add&qty=1&title=" + title + "&id="+id, // Setting request url, which is mapped by StarsServlet in movies.java
        success: handleAddResult // Setting callback function to handle data returned successfully by the StarsServlet
    });
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */
let searchParams = new URLSearchParams(window.location.search);
let movieId = searchParams.get("id"); // Get id from URL

// Makes the HTTP GET request and registers on success callback function handleResult
if(movieId != null) {
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
    });
}
else
    $("#single-movie").text("Movie not found");
