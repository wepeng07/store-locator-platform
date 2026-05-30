package com.mo.store_locator.dto.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminStoreCreateRequest {
    @NotBlank
    @Pattern(regexp = "^S\\d+$", message = "storeId must match S followed by digits")
    private String storeId;

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    @Pattern(
            regexp = AdminStoreValidationPatterns.STORE_TYPE,
            message = "storeType must be one of: Flagship, Urban, Neighborhood"
    )
    private String storeType;

    @NotBlank
    @Size(max = 50)
    @Pattern(
            regexp = AdminStoreValidationPatterns.STATUS,
            message = "status must be one of: active, inactive"
    )
    private String status;

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double latitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double longitude;

    @Valid
    @NotNull
    private AdminStoreAddressRequest address;

    @NotBlank
    @Pattern(regexp = "^[+0-9()\\-\\s]{7,30}$", message = "phone must contain only phone characters")
    private String phone;

    @Pattern(regexp = ".*\\S.*", message = "services must not be blank")
    @Size(max = 1000)
    private String services;

    @Valid
    @NotNull
    private AdminStoreHoursRequest hours;

    @AssertTrue(message = "latitude and longitude must both be provided or both omitted")
    public boolean hasCoordinatePair() {
        return (latitude == null && longitude == null) || (latitude != null && longitude != null);
    }
}
