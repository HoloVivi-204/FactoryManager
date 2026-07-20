package com.factory.management.repository;
import com.factory.management.entity.User;
import com.factory.management.entity.Role;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>{
    @Query("select case when count(u) > 0 then true else false end " +
            "from User u join u.roles r where r = :role")
    boolean existsByRole(@Param("role") Role role);
    @EntityGraph(attributePaths = {
            "roles", "employee", "employee.team", "employee.team.productionLine",
            "employee.team.productionLine.department", "employee.team.productionLine.department.factory"
    })
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmployee_Id(Long employeeId);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmployee_Id(Long employeeId);
}
