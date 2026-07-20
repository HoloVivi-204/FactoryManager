package com.factory.management.entity;
import jakarta.persistence.*;import lombok.*;
@Entity @Table(name="warehouse",uniqueConstraints=@UniqueConstraint(name="uk_warehouse_code",columnNames="code")) @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Warehouse{@Id@GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@Column(nullable=false,length=50)private String code;@Column(nullable=false,length=255)private String name;@ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="factory_id",nullable=false)private Factory factory;@Column(length=500)private String description;@Column(nullable=false)@Builder.Default private Boolean active=true;}
