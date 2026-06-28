package api;

import api.client.OrderClient;
import api.client.UserClient;
import api.model.Order;
import api.model.User;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class OrderTest {
    private UserClient userClient;
    private OrderClient orderClient;
    private String token;
    private List<String> validIngredients;

    @Before
    public void setUp() {
        userClient = new UserClient();
        orderClient = new OrderClient();
        validIngredients = new ArrayList<>();
        try {
            Response response = given()
                    .filter(new AllureRestAssured())
                    .baseUri("https://stellarburgers.education-services.ru/")
                    .when()
                    .get("/api/ingredients");

            if (response.getStatusCode() == 200) {
                List<String> ids = response.path("data._id");
                if (ids != null && !ids.isEmpty()) {
                    validIngredients.add(ids.get(0));
                    if (ids.size() > 1) {
                        validIngredients.add(ids.get(1));
                    }
                }
            }
        } catch (Exception e) {
            validIngredients.clear();
        }

        if (validIngredients.isEmpty()) {
            validIngredients = Arrays.asList("61c0c5cd1d1f82001bda4651", "61c0c5cd1d1f82001bda4652");
        }
    }

    @After
    public void tearDown() {
        if (token != null && !token.isEmpty()) {
            userClient.deleteUser(token);
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    public void createOrderWithAuthAndIngredientsTest() {
        String email = RandomStringUtils.randomAlphanumeric(10).toLowerCase() + "@yandex.ru";
        User user = new User(email, "password123", "Nastya");
        Response userResponse = userClient.createUser(user);
        token = userResponse.path("accessToken");

        Order order = new Order(validIngredients);
        Response response = orderClient.createOrder(order, token);
        response.then().statusCode(200).body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    public void createOrderWithoutAuthTest() {
        Order order = new Order(validIngredients);
        Response response = orderClient.createOrder(order, "");
        response.then().statusCode(200).body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    public void createOrderWithoutIngredientsTest() {
        String email = RandomStringUtils.randomAlphanumeric(10).toLowerCase() + "@yandex.ru";
        User user = new User(email, "password123", "Nastya");
        Response userResponse = userClient.createUser(user);
        token = userResponse.path("accessToken");

        Order order = new Order(new ArrayList<>());
        Response response = orderClient.createOrder(order, token);

        // Исправлено: Добавлена проверка сообщения об ошибке
        response.then().statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    public void createOrderWithWrongHashTest() {
        String email = RandomStringUtils.randomAlphanumeric(10).toLowerCase() + "@yandex.ru";
        User user = new User(email, "password123", "Nastya");
        Response userResponse = userClient.createUser(user);
        token = userResponse.path("accessToken");

        Order order = new Order(Arrays.asList("invalid_hash_123", "invalid_hash_456"));
        Response response = orderClient.createOrder(order, token);
        response.then().statusCode(500);
    }
}