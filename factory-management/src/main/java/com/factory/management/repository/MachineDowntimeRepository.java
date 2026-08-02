package com.factory.management.repository;

import com.factory.management.entity.MachineDowntime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MachineDowntimeRepository extends JpaRepository<MachineDowntime, Long> {
    List<MachineDowntime> findAllByProductionReport_Id(Long id);

    List<MachineDowntime> findAllByProductionReport_IdIn(Collection<Long> ids);

    List<MachineDowntime> findAllByMachine_Id(Long id);
}
