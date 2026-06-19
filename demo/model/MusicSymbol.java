package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MusicSymbol {
    private String symbolClass;
    private String description;
    private Double confidence;
    private String pitch;
    private List<Integer> bbox;
    private String positionType;
    private Integer staffIndex;
}