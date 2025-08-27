package event.eventmanagertask.dto;

import event.eventmanagertask.entity.EventStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventDto(
        @NotNull
        Long id,

        @NotBlank(message = "Название обязательно")
        String name,

        @NotBlank(message = "Id организатора обязательно")
        Long ownerId,

        @Min(value = 1, message = "Количество мест должно быть не менее 1")
        Integer maxPlaces,

        @Min(value = 0, message = "Количество мест должно быть положительно")
        Integer occupiedPlaces,

        @NotNull(message = "Дата обязательна")
        LocalDateTime date,

        @Min(value = 0, message = "Стоимость не может быть отрицательной")
        Integer cost,

        @Min(value = 30, message = "Длительность должна быть не менее 30 минут")
        Integer duration,

        @NotNull(message = "Id локации обязательно")
        Long locationId,

        @NotNull(message = "Статус меропрития обязателен")
        EventStatus status) {
}
