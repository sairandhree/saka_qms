package com.saka.qms.web;

import com.saka.qms.model.NameOfItem;
import com.saka.qms.repository.NameOfItemRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
public class NameOfItemController extends CrudController<NameOfItem, Integer> {
    public NameOfItemController(NameOfItemRepository repository) {
        super(repository);
    }
}
