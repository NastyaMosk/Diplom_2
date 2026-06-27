package api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private List<String> ingredients;

    @JsonProperty("_id")
    private String id; // Исправлен нейминг, спецсимвол убран из имени переменной

    private String status;
    private Integer number;
    private String createdAt;
    private String updatedAt;

    // Конструктор только с ингредиентами (для создания заказа через API)
    public Order(List<String> ingredients) {
        this.ingredients = ingredients;
    }
}