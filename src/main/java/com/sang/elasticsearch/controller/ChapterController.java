package com.sang.elasticsearch.controller;

import com.sang.elasticsearch.bean.Chapter;
import com.sang.elasticsearch.service.ElasticService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/chapter")
public class ChapterController {
    private ElasticService<Chapter> elasticService=new ElasticService<>("chapter");

    @RequestMapping("/query")
    public Chapter query(String id) throws Exception {
        return elasticService.query(id);
    }

    @RequestMapping("/queryAll")
    public List<Chapter> queryAll(@RequestBody Map<String,String> map) throws Exception {
        return elasticService.queryAll(map);
    }
}
