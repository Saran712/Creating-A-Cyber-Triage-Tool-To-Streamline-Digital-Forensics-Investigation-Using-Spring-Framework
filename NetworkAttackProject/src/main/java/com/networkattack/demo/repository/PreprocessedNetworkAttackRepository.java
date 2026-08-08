package com.networkattack.demo.repository;

import com.networkattack.demo.Model.PreprocessedNetworkAttack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreprocessedNetworkAttackRepository extends JpaRepository<PreprocessedNetworkAttack, Long> {
    // You can add custom query methods here if needed
}