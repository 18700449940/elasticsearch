package com.sang.elasticsearch.controller;

import com.sang.elasticsearch.GetDataMain;
import com.sang.elasticsearch.bean.Book;
import com.sang.elasticsearch.service.ElasticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private ElasticService<Book> elasticService;

    @RequestMapping("/query")
    public Book query(String id) throws Exception {
        return elasticService.query(Book.class,id);
    }

    @RequestMapping("/queryAll")
    public List<Book> queryAll(@RequestBody Map<String,String> map) throws Exception {
        return elasticService.queryAll(map);
    }
}
