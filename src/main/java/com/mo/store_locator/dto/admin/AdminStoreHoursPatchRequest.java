package com.mo.store_locator.dto.admin;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminStoreHoursPatchRequest {
    @Pattern(regexp = ".*\\S.*", message = "hours.mon must not be blank")
    @Size(max = 100)
    @Pattern(
            regexp = AdminStoreValidationPatterns.HOURS,
            message = "hours.mon must be 'Closed' or formatted as HH:mm-HH:mm"
    )
    private String mon;

    @Pattern(regexp = ".*\\S.*", message = "hours.tue must not be blank")
    @Size(max = 100)
    @Pattern(
            regexp = AdminStoreValidationPatterns.HOURS,
            message = "hours.tue must be 'Closed' or formatted as HH:mm-HH:mm"
    )
    private String tue;

    @Pattern(regexp = ".*\\S.*", message = "hours.wed must not be blank")
    @Size(max = 100)
    @Pattern(
            regexp = AdminStoreValidationPatterns.HOURS,
            message = "hours.wed must be 'Closed' or formatted as HH:mm-HH:mm"
    )
    private String wed;

    @Pattern(regexp = ".*\\S.*", message = "hours.thu must not be blank")
    @Size(max = 100)
    @Pattern(
            regexp = AdminStoreValidationPatterns.HOURS,
            message = "hours.thu must be 'Closed' or formatted as HH:mm-HH:mm"
    )
    private String thu;

    @Pattern(regexp = ".*\\S.*", message = "hours.fri must not be blank")
    @Size(max = 100)
    @Pattern(
            regexp = AdminStoreValidationPatterns.HOURS,
            message = "hours.fri must be 'Closed' or formatted as HH:mm-HH:mm"
    )
    private String fri;

    @Pattern(regexp = ".*\\S.*", message = "hours.sat must not be blank")
    @Size(max = 100)
    @Pattern(
            regexp = AdminStoreValidationPatterns.HOURS,
            message = "hours.sat must be 'Closed' or formatted as HH:mm-HH:mm"
    )
    private String sat;

    @Pattern(regexp = ".*\\S.*", message = "hours.sun must not be blank")
    @Size(max = 100)
    @Pattern(
            regexp = AdminStoreValidationPatterns.HOURS,
            message = "hours.sun must be 'Closed' or formatted as HH:mm-HH:mm"
    )
    private String sun;

    @AssertTrue(message = "At least one hours field must be provided")
    public boolean hasAnyValue() {
        return mon != null || tue != null || wed != null || thu != null || fri != null || sat != null || sun != null;
    }
}
