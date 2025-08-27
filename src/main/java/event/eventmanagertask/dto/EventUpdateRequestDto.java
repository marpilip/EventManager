package event.eventmanagertask.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

public class EventUpdateRequestDto {
    private String name;

    @Min(value = 1, message = "Количество мест должно быть не менее 1")
    private Integer maxPlaces;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Future(message = "Дата должна быть в будущем")
    private LocalDateTime date;

    @Min(value = 0, message = "Стоимость не может быть отрицательной")
    private Integer cost;

    @Min(value = 30, message = "Длительность должна быть не менее 30 минут")
    private Integer duration;

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
