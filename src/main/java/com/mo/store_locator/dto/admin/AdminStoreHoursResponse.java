package com.mo.store_locator.dto.admin;

public record AdminStoreHoursResponse(
        String mon,
        String tue,
        String wed,
        String thu,
        String fri,
        String sat,
        String sun
) {
}
