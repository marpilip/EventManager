package event.eventmanagertask.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class EventCreateRequestDto {
    @NotBlank(message = "Название обязательно")
    private String name;

    @NotNull(message = "Максимальное количество мест обязательно")
    @Min(value = 1, message = "Количество мест должно быть не менее 1")
    private Integer maxPlaces;

    @NotNull(message = "Дата обязательна")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Future(message = "Дата должна быть в будущем")
    private LocalDateTime date;

    @NotNull(message = "Стоимость обязательна")
    @Min(value = 0, message = "Стоимость не может быть отрицательной")
    private Integer cost;

    @NotNull(message = "Длительность обязательна")
    @Min(value = 30, message = "Длительность должна быть не менее 30 минут")
    private Integer duration;

    @NotNull(message = "ID локации обязателен")
    private Long locationId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaxPlaces() {
        return maxPlaces;
    }

    public void setMaxPlaces(Integer maxPlaces) {
        this.maxPlaces = maxPlaces;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }
}
