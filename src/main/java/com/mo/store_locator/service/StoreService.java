package com.mo.store_locator.service;

import com.mo.store_locator.model.Store;
import com.mo.store_locator.repository.StoreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StoreService {
    private static final int DEFAULT_COORDINATE_SEARCH_LIMIT = 10;
    private static final double DEFAULT_RADIUS_MILES = 10.0;
    private static final double MAX_RADIUS_MILES = 100.0;

    private final StoreRepository storeRepository;
    private final DistanceService distanceService;
    private final GeocodingService geocodingService;

    public StoreService(StoreRepository storeRepository, DistanceService distanceService, GeocodingService geocodingService) {
        this.storeRepository = storeRepository;
        this.distanceService = distanceService;
        this.geocodingService = geocodingService;
    }

    public List<Store> getAllStores() {
        return storeRepository.findAllByOrderByIdAsc();
    }

    public Store getStoreById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Store not found with id " + id
                ));
    }

    public List<Store> searchStoresByCity(String city) {
        List<Store> stores = storeRepository.findByAddressCityIgnoreCaseOrderByIdAsc(city.trim());

        if (stores.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No stores found in city " + city
            );
        }

        return stores;
    }

    public List<Store> searchStoresByPostalCode(
            String postalCode,
            List<String> storeTypes,
            List<String> services
    ) {
        String normalizedPostalCode = postalCode.trim();
        if (normalizedPostalCode.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "postalCode must not be blank"
            );
        }

        List<Store> stores = filterByStoreTypesAndServices(
                storeRepository.findByAddressPostalCodeIgnoreCaseOrderByIdAsc(normalizedPostalCode),
                normalizeFilters(storeTypes),
                normalizeFilters(services)
        );

        if (stores.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No stores found for postalCode " + postalCode
            );
        }

        return stores;
    }

    public List<Store> searchStoresByAddress(
            String address,
            Double radiusMiles,
            Integer limit,
            List<String> storeTypes,
            List<String> services
    ) {
        String normalizedAddress = address.trim();
        if (normalizedAddress.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "address must not be blank"
            );
        }

        GeoCoordinates coordinates = geocodingService.geocode(normalizedAddress)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Unable to geocode address"
                ));

        return searchStoresByCoordinates(
                coordinates.latitude(),
                coordinates.longitude(),
                radiusMiles,
                limit,
                storeTypes,
                services
        );
    }

    public List<Store> searchStoresByCoordinates(
            Double latitude,
            Double longitude,
            Double radiusMiles,
            Integer limit,
            List<String> storeTypes,
            List<String> services
    ) {
        if (latitude == null || longitude == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "latitude and longitude must both be provided"
            );
        }

        validateCoordinates(latitude, longitude);

        double searchRadiusMiles = radiusMiles == null ? DEFAULT_RADIUS_MILES : radiusMiles;
        if (searchRadiusMiles <= 0 || searchRadiusMiles > MAX_RADIUS_MILES) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "radiusMiles must be greater than 0 and less than or equal to 100"
            );
        }

        int resultLimit = limit == null ? DEFAULT_COORDINATE_SEARCH_LIMIT : limit;
        if (resultLimit <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "limit must be greater than 0"
            );
        }

        DistanceService.BoundingBox boundingBox = distanceService.calculateBoundingBox(latitude, longitude, searchRadiusMiles);
        List<Store> candidates = storeRepository.findWithinBoundingBox(
                boundingBox.minLatitude(),
                boundingBox.maxLatitude(),
                boundingBox.minLongitude(),
                boundingBox.maxLongitude()
        );
        List<Store> filteredCandidates = filterByStoreTypesAndServices(
                candidates,
                normalizeFilters(storeTypes),
                normalizeFilters(services)
        );
        List<Store> stores = distanceService.filterSortAndLimitByDistance(
                filteredCandidates,
                latitude,
                longitude,
                searchRadiusMiles,
                resultLimit
        );

        return stores;
    }

    private List<String> normalizeFilters(List<String> filters) {
        if (filters == null) {
            return List.of();
        }

        return filters.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(filter -> !filter.isEmpty())
                .toList();
    }

    private List<Store> filterByStoreTypesAndServices(
            List<Store> stores,
            List<String> storeTypes,
            List<String> services
    ) {
        return stores.stream()
                .filter(store -> storeTypes.isEmpty() || storeTypes.contains(store.getStoreType()))
                .filter(store -> services.isEmpty() || storeServices(store).containsAll(services))
                .toList();
    }

    private Set<String> storeServices(Store store) {
        if (store.getServices() == null || store.getServices().isBlank()) {
            return Set.of();
        }

        return Arrays.stream(store.getServices().split("\\|"))
                .map(String::trim)
                .filter(service -> !service.isEmpty())
                .collect(Collectors.toSet());
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "latitude must be between -90 and 90"
            );
        }

        if (longitude < -180 || longitude > 180) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "longitude must be between -180 and 180"
            );
        }
    }
}
