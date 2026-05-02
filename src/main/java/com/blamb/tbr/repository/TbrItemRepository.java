package com.blamb.tbr.repository;

import com.blamb.tbr.model.Category;
import com.blamb.tbr.model.TbrItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for TbrItem.
 *
 * We don't write the implementation. Spring generates one at startup based on:
 *   1. The interface extending JpaRepository — gives us findAll, findById,
 *      save, deleteById, etc. for free
 *   2. The method names below — Spring parses the names and generates SQL:
 *      findByCategoryOrderByAddedAtDesc(BOOK) becomes roughly
 *        SELECT * FROM tbr_item WHERE category = 'BOOK' ORDER BY added_at DESC
 *
 * This "derived query" mechanism is convenient for simple cases. For anything
 * more complex (joins, projections, dynamic predicates), you'd fall back to
 * @Query annotations or the JdbcClient API.
 */
@Repository
public interface TbrItemRepository extends JpaRepository<TbrItem, Long> {

    List<TbrItem> findByCategoryOrderByAddedAtDesc(Category category);

    List<TbrItem> findAllByOrderByAddedAtDesc();
}
