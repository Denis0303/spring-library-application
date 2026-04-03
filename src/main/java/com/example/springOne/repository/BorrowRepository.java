package com.example.springOne.repository;

import com.example.springOne.model.Book;
import com.example.springOne.model.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    List<Borrow> findByUsername(String username);

    List<Borrow> findByBookAndReturnedFalse(Book book);

    boolean existsByBook(Book book);


    long countByBookAndReturnedFalse(Book book);
    
    List<Borrow> findByBook(Book book);   // 👈 add this
}