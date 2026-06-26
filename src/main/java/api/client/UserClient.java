package api.client;

import api.model.User;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class UserClient {
    private static final String BASE_URL = "https://stellarburgers.education-services.ru/";
    private static final String CREATE_USER_PATH = "/api/auth/register";
    private static final String LOGIN_USER_PATH = "/api/auth/login";
    private static final String USER_PATH = "/api/auth/user";

    @Step("Создание пользователя")
    public Response createUser(User user) {
        return given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .baseUri(BASE_URL)
                .body(user)
                .when()
                .post(CREATE_USER_PATH);
    }

    @Step("Логин пользователя")
    public Response loginUser(User user) {
        return given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .baseUri(BASE_URL)
                .body(user)
                .when()
                .post(LOGIN_USER_PATH);
    }

    @Step("Удаление пользователя")
    public Response deleteUser(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        // Автоматически добавляем Bearer, если его нет в начале строки токена
        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;

        return given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .baseUri(BASE_URL)
                .header("Authorization", authHeader)
                .when()
                .delete(USER_PATH);
    }
}