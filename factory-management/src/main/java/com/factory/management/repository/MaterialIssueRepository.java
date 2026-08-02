package com.factory.management.repository;

import com.factory.management.entity.MaterialIssue;
import com.factory.management.entity.MaterialIssueType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MaterialIssueRepository extends JpaRepository<MaterialIssue, Long> {
    List<MaterialIssue> findAllByProductionReport_Id(Long id);

    List<MaterialIssue> findAllByProductionReport_IdIn(Collection<Long> ids);

    List<MaterialIssue> findAllByMaterial_Id(Long id);

    List<MaterialIssue> findAllByIssueType(MaterialIssueType type);
}
