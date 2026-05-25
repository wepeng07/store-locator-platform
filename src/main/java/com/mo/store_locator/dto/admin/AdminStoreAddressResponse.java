package com.mo.store_locator.dto.admin;

public record AdminStoreAddressResponse(
        String street,
        String city,
        String state,
        String postalCode,
        String country
) {
}
