/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleBrowseResult(resultData) {
    let length = resultData.length;
    let genres_content = jQuery("#genres-content");
    genres_content.html("");

    let searchParams = new URLSearchParams(window.location.search);
    let genre = searchParams.get("genre");
    
    for (let i = 0; i < length; i++) {
        let activeStyle = (genre === resultData[i]['genre_id'])? 'style="color:purple;"': "";
        genres_content.append('<a href="movies.html?genre=' + resultData[i]['genre_id'] + '"'+ activeStyle +'>' + resultData[i]["genre_name"] +'</a>');
    }

    let letters = jQuery('#letters');
    if(letters.length != 0) {
        const alphabets = ["0","1","2","3","4","5","6","7","8","9",
            "A","B","C","D","E","F","G","H","I","J","K", "L","M","N",
            "O", "P", "Q","R","S","T","U", "V","W","X","Y","Z","*"];

        letters.html("");
        for (let i = 0; i < alphabets.length; i++) {
            letters.append('<a href="movies.html?title=' + alphabets[i] + '">'
                            + alphabets[i] +'</a>');
        }
    }

    $(".menu-toggle").click(function() {
        console.log($(".top-navigation").eq(1).css("left"));
        if($(".top-navigation").eq(1).css("left") == "0px")
            $(".top-navigation").eq(1).css("left","-100%");
        else
            $(".top-navigation").eq(1).css("left","0");
    });
}

/**
 * Retrieve the data with POST method
 */
$.ajax(
    "api/browse", {
        method: "POST",
        success: handleBrowseResult
    }
);