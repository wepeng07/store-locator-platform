package com.mo.store_locator.repository;

import com.mo.store_locator.model.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findAllByOrderByIdAsc();

    List<Store> findByAddressCityIgnoreCaseOrderByIdAsc(String addressCity);

    List<Store> findByAddressPostalCodeIgnoreCaseOrderByIdAsc(String addressPostalCode);

    @Query("""
            select store from Store store
            where store.latitude between :minLatitude and :maxLatitude
              and store.longitude between :minLongitude and :maxLongitude
            order by store.id asc
            """)
    List<Store> findWithinBoundingBox(
            @Param("minLatitude") double minLatitude,
            @Param("maxLatitude") double maxLatitude,
            @Param("minLongitude") double minLongitude,
            @Param("maxLongitude") double maxLongitude
    );

    Optional<Store> findByStoreId(String storeId);

    boolean existsByStoreId(String storeId);

    Page<Store> findAllByOrderByStoreIdAsc(Pageable pageable);
}
