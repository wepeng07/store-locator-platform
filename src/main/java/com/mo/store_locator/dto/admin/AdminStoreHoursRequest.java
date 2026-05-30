package com.mo.store_locator.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminStoreHoursRequest {
    @NotBlank
    @Size(max = 100)
    @Pattern(
            regexp = AdminStoreValidationPatterns.HOURS,
            message = "hours.mon must be 'Closed' or formatted as HH:mm-HH:mm"
    )
    private String mon;

    @NotBlank
    @Size(max = 100)
    @Pattern(
            regexp = AdminStoreValidationPatterns.HOURS,
            message = "hours.tue must be 'Closed' or formatted as HH:mm-HH:mm"
    )
    private String tue;

    @NotBlank
    @Size(max = 100)
    @Pattern(
            regexp = AdminStoreValidationPatterns.HOURS,
            message = "hours.wed must be 'Closed' or formatted as HH:mm-HH:mm"
    )
    private String wed;

    @NotBlank
    @Size(max = 100)
    @Pattern(
            regexp = AdminStoreValidationPatterns.HOURS,
            message = "hours.thu must be 'Closed' or formatted as HH:mm-HH:mm"
    )
    private String thu;

    @NotBlank
    @Size(max = 100)
    @Pattern(
            regexp = AdminStoreValidationPatterns.HOURS,
            message = "hours.fri must be 'Closed' or formatted as HH:mm-HH:mm"
    )
    private String fri;

    @NotBlank
    @Size(max = 100)
    @Pattern(
            regexp = AdminStoreValidationPatterns.HOURS,
            message = "hours.sat must be 'Closed' or formatted as HH:mm-HH:mm"
    )
    private String sat;

    @NotBlank
    @Size(max = 100)
    @Pattern(
            regexp = AdminStoreValidationPatterns.HOURS,
            message = "hours.sun must be 'Closed' or formatted as HH:mm-HH:mm"
    )
    private String sun;
}
