package com.blamb.tbr.repository;

import com.blamb.tbr.model.Category;
import com.blamb.tbr.model.TbrItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TbrItemRepository extends JpaRepository<TbrItem, Long> {

    List<TbrItem> findByCategoryOrderByAddedAtDesc(Category category);

    List<TbrItem> findAllByOrderByAddedAtDesc();
}
