package api.model;

public class User {
    private String email;
    private String password;
    private String name;

    // 1. Дефолтный пустой конструктор.
    // ОБЯЗАТЕЛЕН, чтобы RestAssured/Gson могли превращать JSON ответа сервера обратно в объект Java.
    public User() {
    }

    // 2. Полный конструктор со всеми полями.
    // Используется для создания стандартного валидного пользователя.
    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    // 3. Дополнительные конструкторы для негативных тест-кейсов (без обязательных полей).
    // Конструктор без имени (для проверки ошибки создания):
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Геттеры и сеттеры
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}