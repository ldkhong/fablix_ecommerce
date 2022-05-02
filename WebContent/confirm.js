/**
 * Handle the data returned by IndexServlet
 * @param resultDataJson jsonObject, consists of session info
 */

function handleConfirmData(resultDataJson) {
    let listOfItems = resultDataJson["items"];
    let id = parseInt(resultDataJson["lastId"]) + 1;
    let total = 0.0;
    let item_list = $("#item_list_body");

    for (let i = 0; i < listOfItems.length; i++) {

        let qty = listOfItems[i]["quantity"];
        let itemHTML = "";
        let saleId = "";

        for (let j = 0; j < qty; j++) {
            saleId += (id+j).toString() + " ";
        }

        itemHTML += '<tr>' +'<td>'+ saleId + '</td>' +'<td>'+ listOfItems[i]["title"] + '</td>' + '<td>$10</td>' + '<td>' + qty + '</td>';
        item_list.append(itemHTML);
        total += qty*10;
    }

    let price = '<tr><td></td><td></td><td></td><td><h4 id="total">Total: $'+total+'</h4></td></tr>';
    item_list.append(price);

    let goPayment = jQuery("#total_price");
    let payment = "checkout.html?total=" + total;
    $(goPayment).attr("href", payment);
}


/**
 * Submit form content with POST method
 * @param cartEvent
 */


$.ajax("api/confirm", {
    method: "GET",
    success: handleConfirmData
});

