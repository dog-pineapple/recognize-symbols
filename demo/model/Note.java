package com.example.demo.model;
import jakarta.persistence.*;
import lombok.Data;
@Data
@Entity
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    private String name;
    private Integer octave;
    @ManyToOne
    @JoinColumn(name = "note_list_id")
    private NoteList noteList;
    public Note() {

    }
    public Note(String name, Integer octave) {
        this.name = name;
        this.octave = octave;

    }

    public void setNoteList(NoteList noteList) {
        this.noteList = noteList;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOctave() {
        return octave;
    }

    public void setOctave(Integer octave) {
        this.octave = octave;
    }
}