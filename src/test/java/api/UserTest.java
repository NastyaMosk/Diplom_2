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
    private User existingUser; // Общий пользователь для тестов авторизации и дубликатов

    // Вспомогательный метод для генерации случайных данных
    private User generateRandomUser() {
        String email = RandomStringUtils.randomAlphanumeric(10).toLowerCase() + "@yandex.ru";
        return new User(email, "password123", "Nastya");
    }

    @Before
    public void setUp() {
        userClient = new UserClient();

        // Создаем пользователя в системе перед КАЖДЫМ тестом (для тех тестов, где он необходим)
        existingUser = generateRandomUser();
        Response response = userClient.createUser(existingUser);
        token = response.path("accessToken");
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
        // Чтобы тест проверял создание С нуля, генерируем нового юзера
        User newUser = generateRandomUser();

        Response response = userClient.createUser(newUser);
        response.then().statusCode(200)
                .body("success", equalTo(true))
                .body("accessToken", notNullValue());

        // Удаляем именно этого нового пользователя в конце теста
        String newToken = response.path("accessToken");
        if (newToken != null) {
            userClient.deleteUser(newToken);
        }
    }

    @Test
    @DisplayName("Создание пользователя, который уже зарегистрирован")
    public void createExistingUserTest() {
        // Передаем пользователя, который УЖЕ был создан в setUp()
        Response response = userClient.createUser(existingUser);
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
        // Используем пользователя, созданного в setUp()
        Response response = userClient.loginUser(existingUser);
        response.then().statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Вход с неверным логином (email)")
    public void loginWithWrongEmailTest() {
        // Берем валидный пароль предсозданного юзера, но портим email
        User userWithWrongEmail = new User("incorrect_email_999@yandex.ru", existingUser.getPassword(), existingUser.getName());

        Response response = userClient.loginUser(userWithWrongEmail);
        response.then().statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с неверным паролем")
    public void loginWithWrongPasswordTest() {
        // Берем валидный email предсозданного юзера, но портим пароль
        User userWithWrongPassword = new User(existingUser.getEmail(), "wrong_password_abc", existingUser.getName());

        Response response = userClient.loginUser(userWithWrongPassword);
        response.then().statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}
