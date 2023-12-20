package com.example.bookshop.controller;

import com.example.bookshop.entity.Book;
import com.example.bookshop.repository.BookRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/book")
@CrossOrigin(origins = "*")
public class BookController {

    @Autowired
    BookRepository bookRepository;


    @GetMapping("")
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @PostMapping("")
    public Book addBook(@RequestBody Book book) {
        return bookRepository.save(book);
    }

    @PutMapping("/{id}")
    public Book updateBook(@RequestBody Book book) {
        if (bookRepository.existsById( book.getId())) {
                Optional<Book> doc = bookRepository.findById( book.getId());
    //        
                if(book.getTitle() != null) doc.get().setTitle(book.getTitle());
                if(book.getAuthor() != null) doc.get().setAuthor(book.getAuthor());
                if(book.getIsbn() != null) doc.get().setIsbn(book.getIsbn());
                if(book.getPlot() != null) doc.get().setPlot(book.getPlot());
                if(book.getCompleteReads() != null) doc.get().setCompleteReads(book.getCompleteReads());
               
                return bookRepository.save(doc.get());
            } else {
                return null;
            }
    }

    @GetMapping("/{id}")
    public Book getBook(@PathVariable("id") Long id) {
        return bookRepository.findById(id).get();
    }



    //    delete set isDeleted true
    @PutMapping("/delete/{id}")
    public Book deleteBook(@RequestBody Book book) {
            if (bookRepository.existsById( book.getId())) {
                Optional<Book> doc = bookRepository.findById( book.getId());
                doc.get().setIsDeleted(true);
                doc.get().setDeleteBookDate(LocalDate.now());
                return bookRepository.save(doc.get());
            } else {
                return null;
            }
    }

    @PutMapping("/restore/{id}")
    public Book restoreBook(@RequestBody Book book) {
            if (bookRepository.existsById( book.getId())) {
                Optional<Book> doc = bookRepository.findById( book.getId());
                doc.get().setIsDeleted(false);
                doc.get().setDeleteBookDate(null);
                return bookRepository.save(doc.get());
            } else {
                return null;
            }
    }

    // search 
    @GetMapping("/search/{title}")
    public List<Book> searchBook(@PathVariable("title") String title) {
        return bookRepository.findByTitleContaining(title);
    }

}
