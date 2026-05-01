package com.blamb.tbr;

import com.blamb.tbr.model.Category;
import com.blamb.tbr.model.TbrItem;
import com.blamb.tbr.repository.TbrItemRepository;
import com.blamb.tbr.service.TbrItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TbrApplicationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private TbrItemRepository repository;
    @Autowired private TbrItemService service;

    @Test
    void contextLoads() {
        assertThat(repository).isNotNull();
        assertThat(service).isNotNull();
    }

    @Test
    void homepageRenders() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("books", "movies", "music", "totalCount"));
    }

    @Test
    void allThreeCategoryPagesRender() throws Exception {
        mockMvc.perform(get("/books")).andExpect(status().isOk()).andExpect(view().name("books"));
        mockMvc.perform(get("/movies")).andExpect(status().isOk()).andExpect(view().name("movies"));
        mockMvc.perform(get("/music")).andExpect(status().isOk()).andExpect(view().name("music"));
    }

    @Test
    void addFormRenders() throws Exception {
        mockMvc.perform(get("/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("add"))
                .andExpect(model().attributeExists("item", "categories"));
    }

    @Test
    void canPersistAndRetrieveAcrossCategories() {
        TbrItem book = new TbrItem();
        book.setTitle("Test Book"); book.setCreator("Test Author"); book.setCategory(Category.BOOK);

        TbrItem movie = new TbrItem();
        movie.setTitle("Test Movie"); movie.setCategory(Category.MOVIE);

        TbrItem music = new TbrItem();
        music.setTitle("Test Album"); music.setCategory(Category.MUSIC);

        service.save(book);
        service.save(movie);
        service.save(music);

        assertThat(service.findByCategory(Category.BOOK)).extracting("title").contains("Test Book");
        assertThat(service.findByCategory(Category.MOVIE)).extracting("title").contains("Test Movie");
        assertThat(service.findByCategory(Category.MUSIC)).extracting("title").contains("Test Album");
    }

    @Test
    void toggleCompletedFlipsState() {
        TbrItem item = new TbrItem();
        item.setTitle("Toggle Test"); item.setCategory(Category.BOOK);
        TbrItem saved = service.save(item);
        assertThat(saved.isCompleted()).isFalse();

        TbrItem toggled = service.toggleCompleted(saved.getId());
        assertThat(toggled.isCompleted()).isTrue();
    }
}
