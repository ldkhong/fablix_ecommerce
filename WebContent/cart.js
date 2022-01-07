/**
 * Handle the items in item list
 * @param resultArray jsonObject, needs to be parsed to html
 */
function handleSessionData(resultDataJson) {
    //let resultDataJson = JSON.parse(resultDataString);

    /** link previous URL to continue shopping button */
    let previousUrl = resultDataJson["previousUrl"];
    $("#continue-shopping-btn").attr("href", previousUrl);

    /** List all the items that customer has been ordering */
    let listOfItems = resultDataJson["items"]; //array of Items where each element contains 'id', 'title', 'quantity'
    let subtotal = 0.0; // total price of all items

    //empty the HTML of list-items
    let itemsHTML = $("#list-items");
    itemsHTML.html("");

    //show cart is empty if number of items in cart is 0
    if(listOfItems.length == 0) {
        document.getElementsByClassName("empty-cart")[0].style.display = "block";
        $("#checkout-btn").attr("href","#");
    }
    // render the list of items
    else {
        document.getElementsByClassName("empty-cart")[0].style.display = "none";

        for (let i = 0; i < listOfItems.length; i++) {
            // create the class of item for each movie
            let itemHTML = "<div class = \"item\">";

            // the ordered of the item (1, 2 ,3)
            itemHTML += "\n" + "<div class = \"item-ordered\">" + (i + 1) + ".</div>";

            // movie_title
            itemHTML += "<div class=\"description\">\n" + "<span>" + listOfItems[i]["title"] + " </span>\n" + "</div>";

            // Plus Button + quantity of the movie/item + minus Button.
            itemHTML += "<div class = \"quantity\">\n" +
                "<button class=\"btn btn-light plusQ\" type=\"button\" name=\"button\">" + "+" + "</button>" +
                "<input type=\"text\" class=\"qty\" value=\"" + listOfItems[i]["quantity"] + "\">" +
                "<button class=\"btn btn-light minusQ\" type=\"button\" name=\"button\">" + "-" + "</button>\n" +
                "</div>";

            // total-price of current item
            let price = listOfItems[i]["quantity"] * 10;
            itemHTML += "<div class = \"unit-price\"> $" + 10 + "</div>"
            itemHTML += "<div class = \"total-price\"> $" + price + "</div>"

            // delete-btn
            itemHTML += "<div class=\"buttons\">" +
                "<span class=\"delete-btn\">" +
                "<i class=\"fas fa-trash-alt\" id=\"delete-btn-" + i + "\"></i>" +
                "</span>" + "</div>";

            itemsHTML.append(itemHTML);
            document.getElementById("delete-btn-" + i).onclick = function () {
                updateItem(listOfItems[i]["id"], 0, true)
            };

            // add price to total price of all items
            subtotal += price;
        }

        /** add function to onclick event of plus-btn and minus-btn */
        let plusQuantityButtons = document.getElementsByClassName("plusQ");
        for (let i = 0; i < plusQuantityButtons.length; i++) {
            let button = plusQuantityButtons[i];
            button.addEventListener('click', plusQuantity);
        }

        let minusQuantityButtons = document.getElementsByClassName("minusQ");
        for (let i = 0; i < minusQuantityButtons.length; i++) {
            let button = minusQuantityButtons[i];
            button.addEventListener('click', minusQuantity);
        }

        $("#checkout-btn").attr("href","checkout.html");
    }

    document.getElementById("update-cart-btn").onclick = function() {updateCart(listOfItems)};
    document.getElementsByClassName("subtotal")[0].innerHTML = "Subtotal: $" + subtotal;
}

/** Minus Button function */
function minusQuantity(event) {
    event.preventDefault();

    let $input = $(this).closest('div').find('input');
    let value = parseInt($input.val());

    if (value > 1) {
        value = value - 1;
    } else {
        value = 0;
    }

    let total_price = $(this).closest('.item').find('.total-price');
    let price = value * 10;
    total_price.text("$" + price);
    $input.val(value);
}

/**  Plus Button function  */
function plusQuantity(event) {
    event.preventDefault();

    let $input = $(this).closest('div').find('input');
    let value = parseInt($input.val());

    if (value < 100) {
        value = value + 1;
    }
    else {
        value =100;
    }

    let total_price = $(this).closest('.item').find('.total-price');
    let price = value * 10;
    total_price.text("$" + price);
    $input.val(value);
}

/** Update Cart button */
function updateCart(listOfItems) {
    let listOfUpdateQty = document.getElementsByClassName("qty");
    let len = listOfUpdateQty.length - 1;

    for(let i = 0; i < listOfUpdateQty.length - 1; i++) {
        let updateQty = listOfUpdateQty[i].value;
        if(updateQty != listOfItems[i]["quantity"]) {
            console.log(updateQty, listOfItems[i]["quantity"]);
            updateItem(listOfItems[i]["id"], updateQty, false);
        }
    }

    if(len > -1)
        updateItem(listOfItems[len]["id"], listOfUpdateQty[len].value, true);
}

/** Send update quantity of item in the session */
function updateItem(id, qty, isReload) {

    let query = "?type=edit"+"&id="+id+"&qty="+qty;

    //remove the item from cart session
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/items" + query, // Setting request url, which is mapped by ItemsServlet in ItemsServlet.java
      //  success: handleSessionData // Setting callback function to handle data returned successfully by the ItemsServlet
    });

    //Reload the page if an item is removed or cart is updated
    if(isReload) {
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "POST", // Setting request method
            url: "api/cart", // Setting request url, which is mapped by CartServlet in CartServlet.java
            success: (resultData) => handleSessionData(resultData) // Setting callback function to handle data returned successfully by the CartServlet
        });
    }

}

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "POST", // Setting request method
    url: "api/cart" , // Setting request url, which is mapped by CartServlet in CartServlet.java
    success: (resultData) => handleSessionData(resultData) // Setting callback function to handle data returned successfully by the CartServlet
});



