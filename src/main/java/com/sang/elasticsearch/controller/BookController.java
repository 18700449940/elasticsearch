package com.sang.elasticsearch.controller;

import com.sang.elasticsearch.GetDataMain;
import com.sang.elasticsearch.bean.Book;
import com.sang.elasticsearch.service.ElasticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private ElasticService<Book> elasticService;
    @RequestMapping("/query")
    public void query(String id) throws Exception {
        elasticService.query(id);
    }
}
