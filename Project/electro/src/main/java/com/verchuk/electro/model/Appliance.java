package com.verchuk.electro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "appliances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appliance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "power_consumption", precision = 10, scale = 2, nullable = false)
    private BigDecimal powerConsumption;

    @Column(name = "voltage", precision = 5, scale = 2)
    private BigDecimal voltage;

    @Column(name = "current", precision = 10, scale = 2)
    private BigDecimal current;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}

