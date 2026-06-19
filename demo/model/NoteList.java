package com.example.demo.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notelists")
public class NoteList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    private String name;
    private String composer;
    private LocalDateTime createdAt;
    private byte[] image;
    @OneToMany(mappedBy = "noteList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Note> notes = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;
    public NoteList(byte[] image) {
        this.image = image;
        this.createdAt = LocalDateTime.now();
    }

    public List<Note> getNotes() {
        return notes;
    }

    public NoteList() {

    }

    public void setNotes(Note note) {
        notes.add(note);
        note.setNoteList(this);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public String getComposer() {
        return composer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public byte[] getImage() {
        return image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public void addNote(Note newNote) {
        notes.add(newNote);
        newNote.setNoteList(this);
    }
}
