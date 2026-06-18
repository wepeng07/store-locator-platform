package com.mo.store_locator.service;

import com.mo.store_locator.dto.admin.AdminStoreAddressRequest;

import java.util.Optional;

public interface GeocodingService {
    Optional<GeoCoordinates> geocode(AdminStoreAddressRequest addressRequest);

    Optional<GeoCoordinates> geocode(String address);
}
