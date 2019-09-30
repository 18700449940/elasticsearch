package com.sang.elasticsearch.controller;

import com.sang.elasticsearch.GetDataMain;
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
    private GetDataMain getDataMain;


    @RequestMapping("/addTestData")
    public String addTestData() throws Exception {
        getDataMain.addData();
        return "ok";
    }
}
