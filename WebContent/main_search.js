let search_form = $("#search_form");

function submitSearchForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    let title = $("[name = 'title']").val();
    let year = $("[name = 'year']").val();
    let director = $("[name = 'director']").val();
    let star = $("[name = 'star']").val();

    let movieQuery = "movies.html?type=search";
    movieQuery += ((title === "")? "": "&title=" + title);
    movieQuery += ((year === "")? "": "&year=" +year);
    movieQuery += ((director === "")? "": "&director=" + director);
    movieQuery += ((star === "")? "": "&star=" +  star);

    window.location.replace(movieQuery);
}

// Bind the submit action of the form to a handler function
search_form.submit(submitSearchForm);

