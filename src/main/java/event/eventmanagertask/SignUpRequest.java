package event.eventmanagertask;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
        @NotBlank
        @Size(min = 3, max = 50)
        String login,

        @NotBlank
        @Size(min = 6, max = 100)
        String password,

        @Min(18) Integer age) {

}
