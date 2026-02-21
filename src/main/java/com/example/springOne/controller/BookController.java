package com.example.springOne.controller;

import com.example.springOne.model.Book;
import com.example.springOne.model.Borrow;
import com.example.springOne.repository.BookRepository;
import com.example.springOne.repository.BorrowRepository;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class BookController {

    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;

    public BookController(BookRepository bookRepository, BorrowRepository borrowRepository) {
        this.bookRepository = bookRepository;
        this.borrowRepository = borrowRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/books";
    }

    @GetMapping("/books")
    public String listBooks(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        Sort sort = order.equals("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, 5, sort);
        Page<Book> bookPage = keyword.isEmpty()
                ? bookRepository.findAll(pageable)
                : bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                keyword, keyword, pageable);

        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);

        return "books";
    }

    // BORROW FORM
    @GetMapping("/books/borrow/{id}")
    public String borrowForm(@PathVariable Long id, Model model) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) return "redirect:/books?error=bookNotFound";

        boolean isBorrowed =
                !borrowRepository.findByBookAndReturnedFalse(book).isEmpty();

        model.addAttribute("book", book);
        model.addAttribute("isBorrowed", isBorrowed);


        return "borrow-book";
    }

    // SAVE BORROW (max 60 days)
    @PostMapping("/books/borrow/{id}")
    public String borrowBook(@PathVariable Long id,
                             @RequestParam(defaultValue = "14") int days,
                             Authentication auth) {

        if (auth == null) return "redirect:/login";

        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) return "redirect:/books?error=bookNotFound";

        if (!borrowRepository.findByBookAndReturnedFalse(book).isEmpty()) {
            return "redirect:/books?error=alreadyBorrowed";
        }

        if (days < 1) days = 1;
        if (days > 60) days = 60;

        Borrow borrow = new Borrow();
        borrow.setBook(book);
        borrow.setUsername(auth.getName());
        borrow.setBorrowDate(LocalDate.now());
        borrow.setReturnDate(LocalDate.now().plusDays(days));
        borrow.setReturned(false);

        borrowRepository.save(borrow);
        return "redirect:/my-borrows";
    }


    // RETURN BOOK (only owner or admin)
    @PostMapping("/books/return/{borrowId}")
    public String returnBook(@PathVariable Long borrowId, Authentication auth) {
        if (auth == null) return "redirect:/login";

        Borrow borrow = borrowRepository.findById(borrowId).orElse(null);
        if (borrow == null) return "redirect:/my-borrows?error=borrowNotFound";

        boolean isOwner = borrow.getUsername().equals(auth.getName());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) return "redirect:/my-borrows?error=forbidden";

        borrow.setReturned(true);
        borrow.setReturnDate(LocalDate.now());
        borrowRepository.save(borrow);

        return "redirect:/my-borrows";
    }

    @GetMapping("/debug-db")
    @ResponseBody
    public String debugDb() {
        return new java.io.File("library.db").getAbsolutePath();
    }

    @GetMapping("/books/new")
    public String addBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "add-book";
    }


    @PostMapping("/books")
    public String saveBook(@ModelAttribute Book book) {
        bookRepository.save(book);
        return "redirect:/books";
    }

    @GetMapping("/books/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) return "redirect:/books?error=bookNotFound";
        model.addAttribute("book", book);
        return "edit-book";
    }

    @PostMapping("/books/update/{id}")
    public String updateBook(@PathVariable Long id, Book updatedBook) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) return "redirect:/books?error=bookNotFound";

        book.setTitle(updatedBook.getTitle());
        book.setAuthor(updatedBook.getAuthor());
        book.setYear(updatedBook.getYear());
        bookRepository.save(book);

        return "redirect:/books";
    }

    @GetMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            return "redirect:/books?error=bookNotFound";
        }

        // prevent deleting books that have borrow history
        boolean hasBorrows = !borrowRepository.findByBook(book).isEmpty();
        if (hasBorrows) {
            return "redirect:/books?error=bookHasBorrowHistory";
        }

        bookRepository.delete(book);
        return "redirect:/books";
    }

}