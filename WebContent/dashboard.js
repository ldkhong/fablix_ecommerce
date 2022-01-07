let add_star = $("#add_star");
let add_movie = $("#add_movie");
let metadata = $("#metadata");

function handleAddResult(resultDataJson) {
    console.log("handle Star response");
    $("#add_star")[0].reset();
    $("#star_message").text(resultDataJson["message"]);
}

function submitStarForm(formSubmitEvent) {
    console.log("submit add_star form");
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/addStar", {
            method: "GET",
            data: add_star.serialize(),
            success: handleAddResult
        }
    );
    console.log("done");
}


function handleMovieResult(resultDataJson) {
    console.log("handle Movie response");
    $("#add_movie")[0].reset();
    $("#star_message").text(resultDataJson["message"]);
}

function submitMovieForm(formSubmitEvent) {
    console.log("submit add_movie form");
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/addMovie", {
            method: "GET",
            data: add_movie.serialize(),
            success: handleMovieResult
        }
    );
    console.log("done");
}

function handleMetaResult(resultData) {
    console.log("handle Movie response");
    let meta_table = jQuery("#meta_table");
    let table = "";
    for (let i = 0; i < resultData.length; i++) {
        table += "<h4>" + resultData[i]["table"] + "</h4>";
        table += "<table><thead>\n" +
            "<tr style=\"background-color:#abcfe094;\"> <th>Field</th>\n" +
            "        <th>Type</th>\n" +
            "        <th>Null</th>\n" +
            "        <th>Key</th>\n" +
            "        <th>Default</th>\n" +
            "        <th>Extra</th>\n" +
            "    </tr>\n" +
            "    </thead><tbody>";
        for (let j = 0; j < resultData[i]["body"].length; j++){
            table += "<tr>";
            table += "<td>" + resultData[i]["body"][j]["field"] + "</td>";
            table += "<td>" + resultData[i]["body"][j]["type"] + "</td>";
            table += "<td>" + resultData[i]["body"][j]["null"] + "</td>";
            table += "<td>" + resultData[i]["body"][j]["key"] + "</td>";
            table += "<td>" + resultData[i]["body"][j]["default"] + "</td>";
            table += "<td>" + resultData[i]["body"][j]["extra"] + "</td>";
            table += "</tr>";
        }
        table += "</tbody></table>";
    }
    meta_table.append(table);
}

function submitMetaForm(formSubmitEvent) {
    console.log("submit meta form");
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/metadata", {
            method: "GET",
            data: metadata.serialize(),
            success: handleMetaResult
        }
    );
    console.log("done");
}
// Bind the submit action of the form to a handler function
add_star.submit(submitStarForm);
add_movie.submit(submitMovieForm);
metadata.submit(submitMetaForm);