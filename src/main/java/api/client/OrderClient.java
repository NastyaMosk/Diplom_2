package api.client;

import api.model.Order;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured; // Импортируем фильтр Allure
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class OrderClient {
    private static final String BASE_URL = "https://stellarburgers.education-services.ru/";
    private static final String ORDER_PATH = "/api/orders";

    @Step("Создание заказа")
    public Response createOrder(Order order, String token) {
        var requestSpec = given()
                .filter(new AllureRestAssured()) // Добавляем логирование запросов в Allure-отчет
                .contentType(ContentType.JSON)
                .baseUri(BASE_URL);

        if (token != null && !token.isEmpty()) {
            // Если токен уже содержит "Bearer ", используйте просто token.
            // Если приходит только чистый хэш токена, используйте строку ниже:
            String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;
            requestSpec.header("Authorization", authHeader);
        }

        return requestSpec
                .body(order)
                .when()
                .post(ORDER_PATH);
    }
}