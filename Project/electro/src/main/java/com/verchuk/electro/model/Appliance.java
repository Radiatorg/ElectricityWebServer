package com.verchuk.electro.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "appliances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "categories")
@EqualsAndHashCode(exclude = "categories")
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
    @Deprecated // Оставляем для обратной совместимости, используем categories
    private String category;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "width", precision = 10, scale = 2)
    private BigDecimal width; // ширина прибора в см для визуального редактора

    @Column(name = "height", precision = 10, scale = 2)
    private BigDecimal height; // высота прибора в см для визуального редактора

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "appliance_categories",
        joinColumns = @JoinColumn(name = "appliance_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    @JsonIgnore
    private Set<Category> categories = new HashSet<>();
}

