package com.saka.qms.web;

import com.saka.qms.model.Detail;
import com.saka.qms.repository.DetailRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/details")
public class DetailController extends CrudController<Detail, Integer> {
    public DetailController(DetailRepository repository) {
        super(repository);
    }
}
