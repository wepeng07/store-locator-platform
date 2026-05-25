package com.mo.store_locator.service;

import com.mo.store_locator.dto.admin.AdminStoreAddressRequest;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class StubGeocodingService implements GeocodingService {
    private static final Map<String, GeoCoordinates> ADDRESS_BOOK = Map.of(
            "123 main st|boston|ma|02108|usa", new GeoCoordinates(42.3601, -71.0589),
            "456 broadway|new york|ny|10013|usa", new GeoCoordinates(40.7128, -74.0060),
            "789 mass ave|cambridge|ma|02139|usa", new GeoCoordinates(42.3736, -71.1097),
            "1 admin plaza|seattle|wa|98101|usa", new GeoCoordinates(47.6062, -122.3321)
    );

    @Override
    public Optional<GeoCoordinates> geocode(AdminStoreAddressRequest addressRequest) {
        return Optional.ofNullable(ADDRESS_BOOK.get(buildKey(addressRequest)));
    }

    private String buildKey(AdminStoreAddressRequest addressRequest) {
        return String.join("|",
                normalize(addressRequest.getStreet()),
                normalize(addressRequest.getCity()),
                normalize(addressRequest.getState()),
                normalize(addressRequest.getPostalCode()),
                normalize(addressRequest.getCountry())
        );
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
