package com.example.springOne.controller;

import com.example.springOne.model.Borrow;
import com.example.springOne.repository.BorrowRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BorrowController {

    private final BorrowRepository borrowRepository;

    public BorrowController(BorrowRepository borrowRepository) {
        this.borrowRepository = borrowRepository;
    }

    @GetMapping("/my-borrows")
    public String myBorrows(Model model, Authentication auth) {
        if (auth == null) return "redirect:/login";

        model.addAttribute("borrows",
                borrowRepository.findByUsername(auth.getName()));

        return "my-borrows";
    }
}