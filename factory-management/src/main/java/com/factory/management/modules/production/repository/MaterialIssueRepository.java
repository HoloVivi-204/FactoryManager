package com.factory.management.modules.production.repository;

import com.factory.management.modules.production.entity.MaterialIssue;
import com.factory.management.modules.production.entity.MaterialIssueType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialIssueRepository extends JpaRepository<MaterialIssue, Long> {
    List<MaterialIssue> findAllByProductionReport_Id(Long id);

    List<MaterialIssue> findAllByProductionReport_IdIn(Collection<Long> ids);

    List<MaterialIssue> findAllByMaterial_Id(Long id);

    List<MaterialIssue> findAllByIssueType(MaterialIssueType type);
}
