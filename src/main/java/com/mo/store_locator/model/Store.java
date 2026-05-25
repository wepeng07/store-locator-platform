package com.mo.store_locator.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false, unique = true)
    private String storeId;

    private String name;

    @Column(name = "store_type")
    private String storeType;

    private String status;

    private Double latitude;
    private Double longitude;

    @Column(name = "address_street")
    private String addressStreet;

    @Column(name = "address_city")
    private String addressCity;

    @Column(name = "address_state")
    private String addressState;

    @Column(name = "address_postal_code")
    private String addressPostalCode;

    @Column(name = "address_country")
    private String addressCountry;

    private String phone;

    @Column(length = 1000)
    private String services;

    @Column(name = "hours_mon")
    private String hoursMon;

    @Column(name = "hours_tue")
    private String hoursTue;

    @Column(name = "hours_wed")
    private String hoursWed;

    @Column(name = "hours_thu")
    private String hoursThu;

    @Column(name = "hours_fri")
    private String hoursFri;

    @Column(name = "hours_sat")
    private String hoursSat;

    @Column(name = "hours_sun")
    private String hoursSun;
}
