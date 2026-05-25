package com.mo.store_locator.dto.admin;

public record AdminStoreResponse(
        String storeId,
        String name,
        String storeType,
        String status,
        Double latitude,
        Double longitude,
        String phone,
        String services,
        AdminStoreAddressResponse address,
        AdminStoreHoursResponse hours
) {
}
