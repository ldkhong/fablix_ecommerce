
function handleMovieResult(resultData) {
    /** sort form */
    //change select option value
    let limit = $('#limit');
    limit.val(resultData["limit"]);
    limit.on("change", function() {
        let queryParams = new URLSearchParams(window.location.search);
        queryParams.set("limit", $(this).val());
        history.replaceState(null, null, "?"+queryParams.toString());
        window.location.replace(window.location);
    });

    $("#sortTitle").val(resultData["sortTitle"]);
    $("#sortRating").val(resultData["sortRating"]);
    $("#orderBy").val(resultData["orderBy"]);
    $("#sort-form").click(function () { changeOption()});

    /** number of Items shows on cart icon*/
    let totalItems = resultData["totalItems"];
    if(totalItems === 0){
        $("#total-items").css("display", "none");
    }
    else
        $("#total-items").text(totalItems);

    /** Populate movies table */
    let movies = jQuery("#movies-list");
    // Iterate through resultData
    for (let i = 0; i < resultData['movies'].length; i++) {
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "<div class=\"movies-container\">\n<div class=\"row\">"+
                            "<div class=\"left\">" +
                                '<a class="title" href="single-movie.html?id=' + resultData['movies'][i]['id'] + '">' +
                                    resultData['movies'][i]["title"] + ' (' +  resultData['movies'][i]["year"] + ') - ' +
                                    resultData['movies'][i]["director"] + "</a> </div>";

        let rating = (resultData['movies'][i]["rating"] != null)? resultData['movies'][i]["rating"] : "N/A";
            rowHTML += "<div class=\"right\">\n <span>Rating:&nbsp;</span>"+ rating + "</div> </div>";

        let genres = resultData['movies'][i]["genres"];
        rowHTML += "<div class=\"row\">\n<span>Genres: </span>\n";
        if(genres != null) {
            let genre_id_array = genres.split(',');
            for (let j = 0; j < Math.min(3, genre_id_array.length); j++) {
                let id_genre = genre_id_array[j].split(': ');
                rowHTML += '<a class="btn btn-outline-primary" href="movies.html?genre=' + id_genre[0] + '">' + id_genre[1] + '</a>' + "<br>";
            }
        }
        rowHTML += "</div>";

        let stars = resultData['movies'][i]["stars"];
        rowHTML += "<div class=\"row\">\n" +
                        "<div class=\"left\">\n" +
                            "<span>Starring: </span>";
        if(stars != null) {
            let star_id_name = stars.split(',');
            for (let j = 0; j < Math.min(3, star_id_name.length); j++) {
                // Add a link to single-star.html with id passed with GET url parameter
                let id_name = star_id_name[j].split(': ');
                rowHTML += '<a class="btn btn-outline-primary" href="single-star.html?id=' + id_name[0] + '">'
                    + id_name[1] + '</a>';
            }
        }
        rowHTML += "</div>\n" +
                    "<button class=\"btn btn-outline-primary right\" id=\"btn-add-"+ i +"\" >Add</button>\n" +
            "</div>\n" +
        "</div>";

        // Append the row created to the table body, which will refresh the page
        movies.append(rowHTML);
        document.getElementById("btn-add-" + i).onclick = function() {addItems(resultData['movies'][i]["id"], resultData['movies'][i]["title"])};
    }
    /** Pagination */
    let currentPageNumber = parseInt(resultData["page"]); // current page number
    let lastPageNumber = Math.ceil(parseInt(resultData["total"]) / parseInt(resultData["limit"]));// lastPageNumber = total # movies /  # movies per page
    paginationHTML(lastPageNumber, currentPageNumber);
}

function paginationHTML(lastPageNumber, currentPageNumber) {
    if(lastPageNumber === 0) lastPageNumber = 1;
    if(currentPageNumber > lastPageNumber) currentPageNumber = lastPageNumber;
    if(currentPageNumber <= 0) currentPageNumber = 1;

    let pagination = "";
    let searchParams = new URLSearchParams(window.location.search);

    // Previous button
    if (currentPageNumber === 1)
        pagination += '<a><i class="fa fa-angle-left" style="font-size:20px;color:black;"></i></a>';
    else {
        searchParams.set('page', (currentPageNumber - 1).toString());
        pagination += '<a href="movies.html?' + searchParams.toString() + '"><i class="fa fa-angle-left" style="font-size:20px; color:black;"></i></a>';
    }

    let midFirstPage = 1, midLastPage = (lastPageNumber < 6)? lastPageNumber : 6;
    if(currentPageNumber > 6) {
        searchParams.set('page', (1).toString());
        pagination += '<a href="movies.html?' + searchParams.toString() + '">' + 1 + '</a>';
        pagination += '<a style="font-size:20px; color:black;">...</a>';
        midFirstPage = currentPageNumber - 1;
        midLastPage = (lastPageNumber - 1 > currentPageNumber + 2)? (currentPageNumber + 2) : lastPageNumber;
    }

    for (let k = midFirstPage; k <= midLastPage; k++) {
        searchParams.set('page', k.toString());
        if (k === currentPageNumber) {
            pagination += '<a href="movies.html?' + searchParams.toString() + '"' + ' class="active"' + '>' + k + '</a>';
        } else {
            pagination += '<a href="movies.html?' + searchParams.toString() + '">' + k + '</a>';
        }
    }

    if(midLastPage < lastPageNumber) {
        pagination += '<a style="font-size:20px; color:black;">...</a>';
        searchParams.set('page', lastPageNumber.toString());
        pagination += '<a href="movies.html?' + searchParams.toString() + '">' + lastPageNumber + '</a>';
    }

    // Next button
    if (currentPageNumber === lastPageNumber )
        pagination += '<a><i class="fa fa-angle-right" style="font-size:20px; color:black;"></i></a>';
    else {
        searchParams.set('page', (currentPageNumber + 1).toString());
        pagination += '<a href="movies.html?' + searchParams.toString() + '"><i class="fa fa-angle-right" style="font-size:20px; color:black;"></i></a>';
    }

    jQuery("#pagination").append(pagination);
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
    total_items.css("display","block");
    total_items.text(resultDataJson["totalItems"]);
}

function addItems(id,title) {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/items?type=add&qty=1&id="+id+"&title="+title, // Setting request url, which is mapped by StarsServlet in movies.java
        success: handleAddResult // Setting callback function to handle data returned successfully by the StarsServlet
        });
}

function changeOption(){
    let queryParams = new URLSearchParams(window.location.search);

    queryParams.set("sortTitle", $("#sortTitle").val());
    queryParams.set("sortRating", $("#sortRating").val());
    queryParams.set("orderBy",$("#orderBy").val());

    history.replaceState(null, null, "?"+queryParams.toString());
    window.location.replace(window.location);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
let query = window.location.search;
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies" + query, // Setting request url, which is mapped by StarsServlet in movies.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});