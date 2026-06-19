package api;

import api.client.UserClient;
import api.model.User;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class UserTest {
    private UserClient userClient;
    private String token;

    @Before
    public void setUp() {
        userClient = new UserClient();
    }

    @After
    public void tearDown() {
        if (token != null && !token.isEmpty()) {
            userClient.deleteUser(token);
            token = null;
        }
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    public void createUniqueUserTest() {
        String email = RandomStringUtils.randomAlphanumeric(10).toLowerCase() + "@yandex.ru";
        User user = new User(email, "password123", "Nastya");

        Response response = userClient.createUser(user);
        response.then().statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue());

        token = response.path("accessToken");
    }

    @Test
    @DisplayName("Создание пользователя, который уже зарегистрирован")
    public void createExistingUserTest() {
        String email = RandomStringUtils.randomAlphanumeric(10).toLowerCase() + "@yandex.ru";
        User user = new User(email, "password123", "Nastya");

        Response setupResponse = userClient.createUser(user);
        token = setupResponse.path("accessToken");

        Response response = userClient.createUser(user);
        response.then().statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля (email)")
    public void createUserWithoutEmailTest() {
        User user = new User("", "password123", "Nastya");

        Response response = userClient.createUser(user);
        response.then().statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    public void loginExistingUserTest() {
        String email = RandomStringUtils.randomAlphanumeric(10).toLowerCase() + "@yandex.ru";
        User user = new User(email, "password123", "Nastya");

        Response setupResponse = userClient.createUser(user);
        token = setupResponse.path("accessToken");

        Response response = userClient.loginUser(user);
        response.then().statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Вход с неверным логином и паролем")
    public void loginWithWrongCredentialsTest() {
        User user = new User("wrong_email_12345@yandex.ru", "wrong_pass", "NoName");

        Response response = userClient.loginUser(user);
        response.then().statusCode(401)
                .body("success", equalTo(false));
    }
}