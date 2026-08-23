package com.velora.website.Repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.velora.website.Entity.DanhSachDenIP;

public interface DanhSachDenIPRepository extends JpaRepository<DanhSachDenIP, Integer> {
    boolean existsByDiaChiIP(String diaChiIP);
}