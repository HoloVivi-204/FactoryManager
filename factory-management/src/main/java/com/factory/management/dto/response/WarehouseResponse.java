package com.factory.management.dto.response;import lombok.*;
@Getter@Setter@Builder@NoArgsConstructor@AllArgsConstructor public class WarehouseResponse{private Long id;private String code;private String name;private Long factoryId;private String factoryName;private String description;private Boolean active;}
