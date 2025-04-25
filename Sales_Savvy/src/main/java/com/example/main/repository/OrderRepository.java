package com.example.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.main.entity.Orders;

@Repository
public interface OrderRepository extends JpaRepository<Orders, String> {

}
