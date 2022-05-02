let checkout_form = $("#checkout_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataJson jsonObject
 */
function handleCheckoutResult(resultDataJson) {
    //let resultDataJson = JSON.parse(resultDataString);

    console.log("handle Checkout response");
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to movies.html
    if (resultDataJson["status"] === "success") {
       window.location.replace("confirm.html");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#login_error_message").text(resultDataJson["message"]);
    }

}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitCheckoutForm(formSubmitEvent) {
    console.log("submit checkout form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/checkout", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: checkout_form.serialize(),
            success: handleCheckoutResult
        }
    );
}

$(document).ready(function(){
    let urlParams = new URLSearchParams(window.location.search);
    let price = urlParams.get('total');
    let total_price = $('#total_price');
    total_price.attr("value",price);
    total_price.append("$" + price);

})

// Bind the submit action of the form to a handler function
checkout_form.submit(submitCheckoutForm);

