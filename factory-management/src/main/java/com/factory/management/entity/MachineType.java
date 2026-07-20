package com.factory.management.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "machine_type",
        uniqueConstraints = @UniqueConstraint(name = "uk_machine_type_code", columnNames = "code")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
