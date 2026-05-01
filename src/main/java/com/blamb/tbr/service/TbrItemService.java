package com.blamb.tbr.service;

import com.blamb.tbr.model.Category;
import com.blamb.tbr.model.TbrItem;
import com.blamb.tbr.repository.TbrItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TbrItemService {

    private final TbrItemRepository repository;

    public TbrItemService(TbrItemRepository repository) {
        this.repository = repository;
    }

    public List<TbrItem> findAll() {
        return repository.findAllByOrderByAddedAtDesc();
    }

    public List<TbrItem> findByCategory(Category category) {
        return repository.findByCategoryOrderByAddedAtDesc(category);
    }

    public TbrItem findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No TBR item with id " + id));
    }

    public TbrItem save(TbrItem item) {
        return repository.save(item);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public TbrItem toggleCompleted(Long id) {
        TbrItem item = findById(id);
        item.setCompleted(!item.isCompleted());
        return repository.save(item);
    }
}
