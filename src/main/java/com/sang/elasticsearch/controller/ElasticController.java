package com.sang.elasticsearch.controller;

import com.sang.elasticsearch.bean.Book;
import com.sang.elasticsearch.service.ElasticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/elastic")
public class ElasticController {

    @Autowired
    private ElasticService elasticService;

    @RequestMapping("/add")
    public void add(@RequestBody Book book) throws Exception {
        elasticService.add(book);
    }

    @RequestMapping("/query")
    public Book query(String id) {
        return elasticService.query(id);
    }

    @RequestMapping("/queryAll")
    public List<Book> queryAll() throws Exception {
        return elasticService.queryAll();
    }
    @RequestMapping("/deleteAll")
    public void deleteAll() throws Exception {
         elasticService.deleteAll();
    }
}
