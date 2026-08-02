package com.factory.management.modules.auth.repository;

import com.factory.management.modules.auth.entity.DataScopeType;
import com.factory.management.modules.auth.entity.UserDataScope;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDataScopeRepository extends JpaRepository<UserDataScope, Long> {
    List<UserDataScope> findAllByUser_Id(Long userId);
    boolean existsByUser_IdAndScopeTypeAndScopeId(Long userId, DataScopeType type, Long scopeId);
    void deleteAllByUser_Id(Long userId);
}
