package com.mo.store_locator.dto.admin;

import jakarta.validation.constraints.NotBlank;
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
    private String mon;

    @NotBlank
    @Size(max = 100)
    private String tue;

    @NotBlank
    @Size(max = 100)
    private String wed;

    @NotBlank
    @Size(max = 100)
    private String thu;

    @NotBlank
    @Size(max = 100)
    private String fri;

    @NotBlank
    @Size(max = 100)
    private String sat;

    @NotBlank
    @Size(max = 100)
    private String sun;
}
