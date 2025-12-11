package com.example.springOne.api;

import com.example.springOne.model.Book;
import com.example.springOne.repository.BookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books API", description = "JSON REST endpoints for books")
public class BookRestController {

    private final BookRepository bookRepository;

    public BookRestController(BookRepository bookRepository){
        this.bookRepository = bookRepository;
    }

    @Operation(summary = "Get all books")
    @GetMapping
    public List<Book> getBooks(){
        return bookRepository.findAll();
    }

    @Operation(summary = "Save new book")
    @PostMapping
    public Book save(@RequestBody Book book){
        return bookRepository.save(book);
    }

    @Operation(summary = "Delete book by id")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        bookRepository.deleteById(id);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Book> update (@PathVariable Long id, @RequestBody Book updatedBook){
        return bookRepository.findById(id)
                .map(book -> {
                    book.setTitle(updatedBook.getTitle());
                    book.setAuthor(updatedBook.getAuthor());
                    book.setYear(updatedBook.getYear());
                    bookRepository.save(book);
                    return ResponseEntity.ok(book);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/books")
    public String listBooks(Model model,
                            @RequestParam(defaultValue = "id") String sortBy,
                            @RequestParam(defaultValue = "asc") String order) {

        Sort sort = order.equals("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        model.addAttribute("books", bookRepository.findAll(sort));
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("order", order);

        return "books";
    }
}