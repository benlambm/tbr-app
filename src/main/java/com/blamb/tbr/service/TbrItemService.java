package com.blamb.tbr.service;

import com.blamb.tbr.model.Category;
import com.blamb.tbr.model.TbrItem;
import com.blamb.tbr.repository.TbrItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer between the controller and the repository.
 *
 * Why bother, when most methods just delegate to the repository?
 *   - It's the natural home for business logic that's more than a single
 *     repo call (see toggleCompleted below — load, mutate, save).
 *   - It keeps controllers focused on web concerns (HTTP, model attributes,
 *     redirects) and hides JPA from them.
 *   - It's the conventional place to put @Transactional boundaries when
 *     you grow into them.
 *
 * Spring injects the repository via constructor injection (preferred over
 * field injection — keeps the class testable without a Spring context).
 */
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
