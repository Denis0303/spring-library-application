package com.example.springOne.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Book {

    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private String author;
    private int year;

    public Book() {}  // REQUIRED for JPA

    public Book(String title, String author, int year){
        this.title = title;
        this.author = author;
        this.year = year;
    }
}