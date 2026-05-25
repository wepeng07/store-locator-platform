package com.mo.store_locator.repository;

import com.mo.store_locator.model.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findAllByOrderByIdAsc();

    List<Store> findByAddressCityIgnoreCaseOrderByIdAsc(String addressCity);

    Optional<Store> findByStoreId(String storeId);

    boolean existsByStoreId(String storeId);

    Page<Store> findAllByOrderByStoreIdAsc(Pageable pageable);
}
