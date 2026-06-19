package com.example.demo.repositories;

import com.example.demo.model.NoteList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteListRepository extends JpaRepository<NoteList, Long> {

}
