/*
 * This Javascript code uses this library: https://github.com/devbridge/jQuery-Autocomplete
 */

/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 * suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")
    console.log("query: " + query);

    // to check past query results first
    console.log("Checking front-end cache")
    if(sessionStorage.getItem(query) !== null)
    {
        console.log("Getting suggestion list from front-end cache");
        handleLookupAjaxSuccess(sessionStorage.getItem(query), query, doneCallback);
    }

    // sending the HTTP GET request to the Java Servlet endpoint movie-suggestion
    // with the query data
    else {
        console.log("sending AJAX request to backend Java Servlet")
        jQuery.ajax({
            "method": "GET",
            // generate the request url from the query.
            // escape the query string to avoid errors caused by special characters
            "url": "movie-suggestion?query=" + escape(query),
            "success": function (data) {
                // pass the data, query, and doneCallback function into the success handler
                console.log("Getting suggestion list from back-end server")
                handleLookupAjaxSuccess(data, query, doneCallback)
            },
            "error": function (errorData) {
                console.log("lookup ajax error")
                console.log(errorData)
            }
        })
    }
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")

    // parse the string into JSON
    var jsonData = JSON.parse(data); // maximum 100 records
    console.log(jsonData)

    // TODO: if you want to cache the result into a global variable you can do it here
    if(sessionStorage.getItem(query) === null) {
        if(sessionStorage.length >= 10)
            sessionStorage.removeItem(sessionStorage.key(0));
        sessionStorage.setItem(query, data);
    }

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    // the "Response Format" section in documentation
    // limit to 10 suggestion from jsonData which has a maximum of 100 records
    doneCallback( { suggestions: jsonData } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieID"])
    window.location.replace('single-movie.html?id=' + suggestion["data"]["movieID"]);
}


/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    deferRequestBy: 300, // set delay time

    //  add minimum characters
    minChars: 3, // set minimum characters
});


/*
 * do normal full text search if no suggestion is selected
 */
function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    // generate query and pass it to movies.js which use AJAX (RESTful API) to do the full-text search.
    let movieQuery = "movies.html?type=fullText";
    if(query != "")
        movieQuery += "&title=" + query;

    window.location.replace(movieQuery);

}

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#autocomplete').val())
    }
});

// bind the onClick event of that button
$("#btn_auto").click (function() {
    handleNormalSearch($('#autocomplete').val())
});



