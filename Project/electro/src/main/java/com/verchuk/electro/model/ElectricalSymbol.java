package com.verchuk.electro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "electrical_symbols")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElectricalSymbol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "svg_path", length = 2000)
    private String svgPath; // SVG path для отрисовки символа

    @Column(nullable = false, length = 50)
    private String type; // "outlet", "switch", "light", "panel", "junction_box"

    @Column(length = 50)
    private String category; // "power", "lighting", "control", "distribution"

    @Column(name = "default_width")
    @Builder.Default
    private Double defaultWidth = 20.0; // ширина по умолчанию в пикселях

    @Column(name = "default_height")
    @Builder.Default
    private Double defaultHeight = 20.0; // высота по умолчанию в пикселях

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}

