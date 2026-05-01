package com.blamb.tbr.controller;

import com.blamb.tbr.model.Category;
import com.blamb.tbr.model.TbrItem;
import com.blamb.tbr.service.TbrItemService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TbrController {

    private final TbrItemService service;

    public TbrController(TbrItemService service) {
        this.service = service;
    }

    // -------- Home: three-section overview --------

    @GetMapping("/")
    public String home(Model model) {
        List<TbrItem> all = service.findAll();
        model.addAttribute("books",  filter(all, Category.BOOK));
        model.addAttribute("movies", filter(all, Category.MOVIE));
        model.addAttribute("music",  filter(all, Category.MUSIC));
        model.addAttribute("totalCount", all.size());
        return "index";
    }

    private List<TbrItem> filter(List<TbrItem> items, Category cat) {
        return items.stream()
                .filter(i -> i.getCategory() == cat)
                .limit(8) // homepage previews top 8 per category
                .collect(Collectors.toList());
    }

    // -------- Category subpages --------

    @GetMapping("/books")
    public String books(Model model) {
        model.addAttribute("items", service.findByCategory(Category.BOOK));
        return "books";
    }

    @GetMapping("/movies")
    public String movies(Model model) {
        model.addAttribute("items", service.findByCategory(Category.MOVIE));
        return "movies";
    }

    @GetMapping("/music")
    public String music(Model model) {
        model.addAttribute("items", service.findByCategory(Category.MUSIC));
        return "music";
    }

    // -------- Create / Edit --------

    @GetMapping("/add")
    public String addForm(@RequestParam(required = false) String category, Model model) {
        TbrItem item = new TbrItem();
        if (category != null) {
            try { item.setCategory(Category.valueOf(category.toUpperCase())); }
            catch (IllegalArgumentException ignored) { /* leave null */ }
        }
        model.addAttribute("item", item);
        model.addAttribute("categories", Category.values());
        model.addAttribute("formMode", "Add");
        return "add";
    }

    @PostMapping("/add")
    public String addSubmit(@Valid @ModelAttribute("item") TbrItem item,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Category.values());
            model.addAttribute("formMode", "Add");
            return "add";
        }
        service.save(item);
        redirectAttributes.addFlashAttribute("flash",
                "Added \"" + item.getTitle() + "\" to your " + item.getCategory().getDisplayName() + " list.");
        return "redirect:/" + item.getCategory().getSlug();
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("item", service.findById(id));
        model.addAttribute("categories", Category.values());
        model.addAttribute("formMode", "Edit");
        return "add"; // reuse the same form template
    }

    @PostMapping("/edit/{id}")
    public String editSubmit(@PathVariable Long id,
                             @Valid @ModelAttribute("item") TbrItem item,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", Category.values());
            model.addAttribute("formMode", "Edit");
            return "add";
        }
        item.setId(id);
        service.save(item);
        redirectAttributes.addFlashAttribute("flash", "Updated \"" + item.getTitle() + "\".");
        return "redirect:/" + item.getCategory().getSlug();
    }

    // -------- Delete / Toggle --------

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        TbrItem item = service.findById(id);
        String slug = item.getCategory().getSlug();
        service.delete(id);
        redirectAttributes.addFlashAttribute("flash", "Deleted \"" + item.getTitle() + "\".");
        return "redirect:/" + slug;
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id) {
        TbrItem item = service.toggleCompleted(id);
        return "redirect:/" + item.getCategory().getSlug();
    }
}
