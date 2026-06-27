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

    // Вспомогательный метод для генерации случайного уникального пользователя
    private User generateRandomUser() {
        String email = RandomStringUtils.randomAlphanumeric(10).toLowerCase() + "@yandex.ru";
        return new User(email, "password123", "Nastya");
    }

    // Вспомогательный метод предварительного создания пользователя в системе
    private User registerTestUser() {
        User user = generateRandomUser();
        Response response = userClient.createUser(user);
        token = response.path("accessToken");
        return user;
    }

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
        User user = generateRandomUser();

        Response response = userClient.createUser(user);
        response.then().statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue());

        token = response.path("accessToken");
    }

    @Test
    @DisplayName("Создание пользователя, который уже зарегистрирован")
    public void createExistingUserTest() {
        User user = registerTestUser(); // Вынесли создание во вспомогательный метод

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
    @DisplayName("Создание пользователя без обязательного поля (password)")
    public void createUserWithoutPasswordTest() {
        User user = new User("test_email@yandex.ru", "", "Nastya");

        Response response = userClient.createUser(user);
        response.then().statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля (name)")
    public void createUserWithoutNameTest() {
        User user = new User("test_email@yandex.ru", "password123", "");

        Response response = userClient.createUser(user);
        response.then().statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    public void loginExistingUserTest() {
        User user = registerTestUser(); // Вынесли создание во вспомогательный метод

        Response response = userClient.loginUser(user);
        response.then().statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Вход с неверным логином (email)")
    public void loginWithWrongEmailTest() {
        User validUser = registerTestUser(); // Регистрируем пользователя с валидным паролем
        User userWithWrongEmail = new User("incorrect_email_999@yandex.ru", validUser.getPassword(), validUser.getName());

        Response response = userClient.loginUser(userWithWrongEmail);
        response.then().statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect")); // Добавили проверку тела ошибки
    }

    @Test
    @DisplayName("Вход с неверным паролем")
    public void loginWithWrongPasswordTest() {
        User validUser = registerTestUser(); // Регистрируем пользователя с валидным email
        User userWithWrongPassword = new User(validUser.getEmail(), "wrong_password_abc", validUser.getName());

        Response response = userClient.loginUser(userWithWrongPassword);
        response.then().statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect")); // Добавили проверку тела ошибки
    }
}