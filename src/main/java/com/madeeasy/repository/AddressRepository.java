package com.madeeasy.repository;

import com.madeeasy.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, String> {
}
