package com.verchuk.electro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "electrical_points")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ElectricalPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_plan_id", nullable = false)
    private FloorPlan floorPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "electrical_symbol_id", nullable = false)
    private ElectricalSymbol electricalSymbol;

    @Column(name = "position_x", nullable = false, precision = 10, scale = 2)
    private BigDecimal positionX; // позиция X в см

    @Column(name = "position_y", nullable = false, precision = 10, scale = 2)
    private BigDecimal positionY; // позиция Y в см

    @Column(name = "height_from_floor", precision = 5, scale = 2)
    private BigDecimal heightFromFloor; // высота от пола в см

    @Column(name = "rotation", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal rotation = BigDecimal.ZERO; // угол поворота в градусах

    @Column(name = "group_name", length = 100)
    private String group; // группа для группового управления (например, "освещение_кухня")

    @Column(name = "notes", length = 500)
    private String notes;
}

