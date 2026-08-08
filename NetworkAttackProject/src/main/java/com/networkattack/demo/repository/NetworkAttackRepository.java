package com.networkattack.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.networkattack.demo.Model.NetworkAttack;

public interface NetworkAttackRepository extends JpaRepository<NetworkAttack, Long> {
}