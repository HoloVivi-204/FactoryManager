package com.factory.management.dto.request;import jakarta.validation.constraints.*;import lombok.*;
@Getter@Setter@NoArgsConstructor@AllArgsConstructor public class WarehouseRequest{@NotBlank@Size(max=50)private String code;@NotBlank@Size(max=255)private String name;@NotNull private Long factoryId;@Size(max=500)private String description;private Boolean active;}
