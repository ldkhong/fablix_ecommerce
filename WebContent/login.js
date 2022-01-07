/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
let login_form = $("#login-form");

function handleLoginResult(resultDataJson) {
    if (resultDataJson["status"] === "success") {
        window.location.replace(resultDataJson["previousURL"]);
    } else {
        if(resultDataJson["message"] != null)
            $("#message").text(resultDataJson["message"]);

        if (resultDataJson["username"] != null)
            setErrorFor($("#username"), "Username cannot be empty")
        else
            $("#username").parent().attr("class", "login-control");

        if (resultDataJson["password"] != null)
            setErrorFor($("#password"), "Password cannot be empty");
        else
            $("#password").parent().attr("class", "login-control");
        $("#isLogin").val("false");
        window.grecaptcha.reset();
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/login", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: login_form.serialize(),
            success: handleLoginResult
        }
    );
}

function setErrorFor(input, message) {
    let formControl = input.parent();
    let small = formControl.children("small");

    small.text(message);
    formControl.attr("class", "login-control error");
}

// Bind the submit action of the form to a handler function
$("#login").on('click', function(){
   $("#isLogin").val("true");
});

login_form.submit(submitLoginForm);