package com.example.demo.model;
import lombok.Data;
import java.util.List;
import java.util.Map;
@Data
public class RecognitionResponse {
    private Boolean success;
    private Integer symbolsCount;

    private List<MusicSymbol> symbols;
    private List<Note> staves;
    private Map<String, Integer> statistics;
    private String error;
}