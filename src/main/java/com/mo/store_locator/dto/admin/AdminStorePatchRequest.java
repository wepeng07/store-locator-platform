package com.mo.store_locator.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminStorePatchRequest {
    @Pattern(regexp = ".*\\S.*", message = "name must not be blank")
    @Size(max = 255)
    private String name;

    @Pattern(regexp = ".*\\S.*", message = "phone must not be blank")
    @Pattern(regexp = "^[+0-9()\\-\\s]{7,30}$", message = "phone must contain only phone characters")
    private String phone;

    @Pattern(regexp = ".*\\S.*", message = "services must not be blank")
    @Size(max = 1000)
    private String services;

    @Pattern(regexp = ".*\\S.*", message = "status must not be blank")
    @Size(max = 50)
    @Pattern(
            regexp = AdminStoreValidationPatterns.STATUS,
            message = "status must be one of: active, inactive"
    )
    private String status;

    @Valid
    private AdminStoreHoursPatchRequest hours;

    @AssertTrue(message = "At least one updatable field must be provided")
    public boolean hasAnyUpdatableField() {
        return name != null || phone != null || services != null || status != null || hours != null;
    }
}
