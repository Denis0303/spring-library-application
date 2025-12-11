package com.example.springOne.controller;

import com.example.springOne.model.Book;
import com.example.springOne.model.Borrow;
import com.example.springOne.repository.BookRepository;
import com.example.springOne.repository.BorrowRepository;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class BookController {

    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;

    public BookController(BookRepository bookRepository, BorrowRepository borrowRepository) {
        this.bookRepository = bookRepository;
        this.borrowRepository = borrowRepository;
    }

    // Redirect root to /books
    @GetMapping("/")
    public String home() {
        return "redirect:/books";
    }

    // LIST BOOKS + SEARCH + SORT + PAGINATION
    @GetMapping("/books")
    public String listBooks(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {

        Sort sort = order.equals("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, 5, sort);
        Page<Book> bookPage;

        if (keyword.isEmpty()) {
            bookPage = bookRepository.findAll(pageable);
        } else {
            bookPage = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                    keyword, keyword, pageable
            );
        }

        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);

        return "books";
    }

    // ADD BOOK FORM
    @GetMapping("/books/new")
    public String createForm(Model model) {
        model.addAttribute("book", new Book());
        return "add-book";
    }

    // SAVE BOOK
    @PostMapping("/books")
    public String saveBook(Book book) {
        bookRepository.save(book);
        return "redirect:/books?page=0";
    }

    // EDIT BOOK FORM
    @GetMapping("/books/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookRepository.findById(id).orElseThrow());
        return "edit-book";
    }

    // UPDATE BOOK
    @PostMapping("/books/update/{id}")
    public String updateBook(@PathVariable Long id, Book updatedBook) {
        Book book = bookRepository.findById(id).orElseThrow();
        book.setTitle(updatedBook.getTitle());
        book.setAuthor(updatedBook.getAuthor());
        book.setYear(updatedBook.getYear());
        bookRepository.save(book);
        return "redirect:/books?page=0";
    }

    // DELETE BOOK
    @GetMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookRepository.deleteById(id);
        return "redirect:/books?page=0";
    }

    // ----------------------------
    //  BORROW A BOOK
    // ----------------------------
    @GetMapping("/books/borrow/{id}")
    public String borrowBook(@PathVariable Long id, Authentication auth) {

        Book book = bookRepository.findById(id).orElseThrow();

        // Check if book is already borrowed
        boolean isBorrowed = !borrowRepository.findByBookAndReturnedFalse(book).isEmpty();
        if (isBorrowed) {
            return "redirect:/books?error=alreadyBorrowed";
        }

        Borrow borrow = new Borrow();
        borrow.setBook(book);
        borrow.setUsername(auth.getName());
        borrow.setBorrowDate(java.time.LocalDate.now());
        borrow.setReturned(false);

        borrowRepository.save(borrow);

        return "redirect:/books?borrowed=success";
    }

    // ----------------------------
    //  RETURN A BOOK
    // ----------------------------
    @GetMapping("/books/return/{borrowId}")
    public String returnBook(@PathVariable Long borrowId) {

        Borrow borrow = borrowRepository.findById(borrowId).orElseThrow();
        borrow.setReturned(true);
        borrow.setReturnDate(java.time.LocalDate.now());

        borrowRepository.save(borrow);

        return "redirect:/my-borrows?returned=success";
    }

    // ----------------------------
    //  SHOW USER BORROW HISTORY
    // ----------------------------
    @GetMapping("/my-borrows")
    public String myBorrows(Model model, Authentication auth) {

        model.addAttribute("borrows", borrowRepository.findByUsername(auth.getName()));
        return "my-borrows";
    }
}

