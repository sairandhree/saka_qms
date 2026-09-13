package com.saka.qms.repository;

import com.saka.qms.model.NameOfItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NameOfItemRepository extends JpaRepository<NameOfItem, Integer> {
    List<NameOfItem> findByNameOfItemContainingIgnoreCase(String name);
}
