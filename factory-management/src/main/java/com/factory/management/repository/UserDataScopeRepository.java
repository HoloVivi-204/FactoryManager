package com.factory.management.repository;

import com.factory.management.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserDataScopeRepository extends JpaRepository<UserDataScope, Long> {
    List<UserDataScope> findAllByUser_Id(Long userId);
    boolean existsByUser_IdAndScopeTypeAndScopeId(Long userId, DataScopeType type, Long scopeId);
    void deleteAllByUser_Id(Long userId);
}
