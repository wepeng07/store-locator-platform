package com.mo.store_locator.service;

import com.mo.store_locator.model.Store;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class DistanceService {
    private static final double EARTH_RADIUS_MILES = 3958.7613;
    private static final double MILES_PER_DEGREE_LATITUDE = 69.0;

    public BoundingBox calculateBoundingBox(double latitude, double longitude, double radiusMiles) {
        double latitudeDelta = radiusMiles / MILES_PER_DEGREE_LATITUDE;
        double longitudeDelta = radiusMiles / (MILES_PER_DEGREE_LATITUDE * Math.cos(Math.toRadians(latitude)));

        return new BoundingBox(
                latitude - latitudeDelta,
                latitude + latitudeDelta,
                longitude - longitudeDelta,
                longitude + longitudeDelta
        );
    }

    public double calculateDistanceMiles(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        double latitudeDistance = Math.toRadians(endLatitude - startLatitude);
        double longitudeDistance = Math.toRadians(endLongitude - startLongitude);
        double startLatitudeRadians = Math.toRadians(startLatitude);
        double endLatitudeRadians = Math.toRadians(endLatitude);

        double haversine = Math.sin(latitudeDistance / 2) * Math.sin(latitudeDistance / 2)
                + Math.cos(startLatitudeRadians) * Math.cos(endLatitudeRadians)
                * Math.sin(longitudeDistance / 2) * Math.sin(longitudeDistance / 2);

        return 2 * EARTH_RADIUS_MILES * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
    }

    public List<Store> filterSortAndLimitByDistance(
            List<Store> candidates,
            double latitude,
            double longitude,
            double radiusMiles,
            int limit
    ) {
        return candidates.stream()
                .filter(store -> store.getLatitude() != null && store.getLongitude() != null)
                .map(store -> new StoreDistance(
                        store,
                        calculateDistanceMiles(latitude, longitude, store.getLatitude(), store.getLongitude())
                ))
                .filter(storeDistance -> storeDistance.distanceMiles() <= radiusMiles)
                .sorted(Comparator.comparingDouble(StoreDistance::distanceMiles))
                .limit(limit)
                .map(StoreDistance::store)
                .toList();
    }

    public record BoundingBox(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
    }

    private record StoreDistance(Store store, double distanceMiles) {
    }
}
