/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResult(resultData) {
    let born = (resultData["starYear"] == null)? "N/A": resultData["starYear"];

    let starHTML = "<div class=\"single-title\">" +resultData["starName"] +"</div>\n" +
                    "<div class=\"single-line year\">Born: " + born + "</div>";

    starHTML += "<div class=\"single-list\">";
    if(resultData["movies"].length > 0){
        starHTML += "<span>Movies:</span>";
        starHTML += "<ul>";
        for(let i = 0; i < resultData["movies"].length; i++) {
            starHTML += "<li class='movies'> <a href=\"single-movie.html?id="+ resultData["movies"][i]["movieId"]+ "\">" +
                resultData["movies"][i]["movieTitle"] + " (" + resultData["movies"][i]["movieYear"] +") </a> </li>";
        }
        starHTML += "</ul>";
    }
    else
        starHTML += "<span>Movies: No Information</span>";

    starHTML += "</div>";
    $("#single-star").append(starHTML);
    $("#go-back-btn").attr("href", resultData["previousURL"]);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */
let searchParams = new URLSearchParams(window.location.search);
let starId = searchParams.get("id"); // Get id from URL

// Makes the HTTP GET request and registers on success callback function handleResult
if(starId != null) {
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
    });
}
else
    $("#single-star").text("Star not found");
