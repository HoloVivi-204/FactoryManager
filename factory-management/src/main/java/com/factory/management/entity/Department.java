package com.factory.management.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "department",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_department_factory_code",
                        columnNames = {"factory_id", "code"}
                ),
                @UniqueConstraint(
                        name = "uk_department_factory_type",
                        columnNames = {"factory_id", "department_type"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "department_type", nullable = false, length = 50)
    private DepartmentType departmentType;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "factory_id", nullable = false)
    private Factory factory;
}
