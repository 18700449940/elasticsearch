package com.sang.elasticsearch.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sang.elasticsearch.bean.ESEntity;
import com.sang.elasticsearch.util.EsUtil;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ElasticService<T> {
    private static final Logger logger = LoggerFactory.getLogger(ElasticService.class);

    private String indexName;
    public ElasticService(String indexName)
    {
        this.indexName=indexName;
    }
    public void add(ESEntity entity) throws Exception {
        // 1、创建索引请求
        IndexRequest request = new IndexRequest(indexName);
        //文档id
        request.id(entity.getId());
        // 2、准备文档数据
        request.source(JSON.toJSONString(entity), XContentType.JSON);

        //4、发送请求
        IndexResponse indexResponse = null;
        try {
            // 同步方式
            indexResponse = EsUtil.index(request);
        } catch (ElasticsearchException e) {
            // 捕获，并处理异常
            //判断是否版本冲突、create但文档已存在冲突
            if (e.status() == RestStatus.CONFLICT) {
                logger.error("冲突了，请在此写冲突处理逻辑！\n" + e.getDetailedMessage());
            }

            logger.error("索引异常", e);
        }
        //5、处理响应
        if (indexResponse != null) {
            String index = indexResponse.getIndex();
            String type = indexResponse.getType();
            String id = indexResponse.getId();
            long version = indexResponse.getVersion();
            if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                System.out.println("新增文档成功，处理逻辑代码写到这里。");
            } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                System.out.println("修改文档成功，处理逻辑代码写到这里。");
            }
            // 分片处理信息
            ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
            if (shardInfo.getTotal() != shardInfo.getSuccessful()) {

            }
            // 如果有分片副本失败，可以获得失败原因信息
            if (shardInfo.getFailed() > 0) {
                for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                    String reason = failure.reason();
                    System.out.println("副本失败原因：" + reason);
                }
            }
        }

        logger.info("Employee add");
    }

    public T query(String id) throws Exception {
        logger.info("Employee query");
        GetRequest request = new GetRequest(
                indexName,
                id);
        GetResponse response = EsUtil.get(request);
        return (T) JSONObject.parse(response.getSourceAsString());
    }

    public List<T> queryAll(Map<String,String>  map) throws Exception {
        SearchRequest searchRequest = new SearchRequest(indexName);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //构造QueryBuilder
            /*QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("user", "kimchy")
                    .fuzziness(Fuzziness.AUTO)
                    .prefixLength(3)
                    .maxExpansions(10);
            sourceBuilder.query(matchQueryBuilder);*/
        for(Map.Entry<String,String> entry:map.entrySet())
        {
            sourceBuilder.query(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
        }

        sourceBuilder.from(0);
        sourceBuilder.size(10);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //是否返回_source字段
        //sourceBuilder.fetchSource(false);

        //设置返回哪些字段
            /*String[] includeFields = new String[] {"title", "user", "innerObject.*"};
            String[] excludeFields = new String[] {"_type"};
            sourceBuilder.fetchSource(includeFields, excludeFields);*/

        //指定排序
        //sourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
        //sourceBuilder.sort(new FieldSortBuilder("_uid").order(SortOrder.ASC));

        // 设置返回 profile
        //sourceBuilder.profile(true);

        //将请求体加入到请求中
        searchRequest.source(sourceBuilder);

        //3、发送请求
        SearchResponse searchResponse = EsUtil.search(searchRequest);


        //4、处理响应
        //搜索结果状态信息
        RestStatus status = searchResponse.status();
        TimeValue took = searchResponse.getTook();
        Boolean terminatedEarly = searchResponse.isTerminatedEarly();
        boolean timedOut = searchResponse.isTimedOut();

        //分片搜索情况
        int totalShards = searchResponse.getTotalShards();
        int successfulShards = searchResponse.getSuccessfulShards();
        int failedShards = searchResponse.getFailedShards();
        for (ShardSearchFailure failure : searchResponse.getShardFailures()) {
            // failures should be handled here
        }

        //处理搜索命中文档结果
        SearchHits hits = searchResponse.getHits();

        long totalHits = hits.getTotalHits().value;
        float maxScore = hits.getMaxScore();

        SearchHit[] searchHits = hits.getHits();
        List<T> entitys = new LinkedList<>();
        for (SearchHit hit : searchHits) {
            // do something with the SearchHit

            String index = hit.getIndex();
            String type = hit.getType();
            String id = hit.getId();
            float score = hit.getScore();

            //取_source字段值
            String sourceAsString = hit.getSourceAsString(); //取成json串
            Map<String, Object> sourceAsMap = hit.getSourceAsMap(); // 取成map对象
            //从map中取字段值
                /*
                String documentTitle = (String) sourceAsMap.get("title");
                List<Object> users = (List<Object>) sourceAsMap.get("user");
                Map<String, Object> innerObject = (Map<String, Object>) sourceAsMap.get("innerObject");
                */
            logger.info("index:" + index + "  type:" + type + "  id:" + id);
            logger.info(sourceAsString);
            entitys.add((T)JSONObject.parse(sourceAsString));
            //取高亮结果
                /*Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                HighlightField highlight = highlightFields.get("title");
                Text[] fragments = highlight.fragments();
                String fragmentString = fragments[0].string();*/
        }

        // 获取聚合结果
            /*
            Aggregations aggregations = searchResponse.getAggregations();
            Terms byCompanyAggregation = aggregations.get("by_company");
            Bucket elasticBucket = byCompanyAggregation.getBucketByKey("Elastic");
            Avg averageAge = elasticBucket.getAggregations().get("average_age");
            double avg = averageAge.getValue();
            */

        // 获取建议结果
            /*Suggest suggest = searchResponse.getSuggest();
            TermSuggestion termSuggestion = suggest.getSuggestion("suggest_user");
            for (TermSuggestion.Entry entry : termSuggestion.getEntries()) {
                for (TermSuggestion.Entry.Option option : entry) {
                    String suggestText = option.getText().string();
                }
            }
            */
        return entitys;
    }

    public void deleteAll() throws Exception {
//        List<ESEntity> entitys = queryAll();
//        for (ESEntity entity : entitys) {
//            delete(entity.getId());
//        }
    }

    public void delete(String indexName,String id) throws Exception {
        DeleteRequest request = new DeleteRequest(
                indexName,//索引
                id);//文档ID

        //===============================可选参数====================================
//        request.routing("routing");//设置routing值
//        request.parent("parent");//设置parent值

        //设置超时：等待主分片变得可用的时间
        request.timeout(TimeValue.timeValueMinutes(2));//TimeValue方式
        request.timeout("1s");//字符串方式

        //刷新策略
//        request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);//WriteRequest.RefreshPolicy实例方式
//        request.setRefreshPolicy("wait_for");//字符串方式
//
//        request.version(2);//设置版本
//        request.versionType(VersionType.EXTERNAL);//设置版本类型

        //同步执行
        DeleteResponse deleteResponse = EsUtil.delete(request);


        //Delete Response
        //返回的DeleteResponse允许检索有关执行操作的信息，如下所示：
        String index = deleteResponse.getIndex();
        String type = deleteResponse.getType();
        long version = deleteResponse.getVersion();
        ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
        if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
            //处理成功分片数量少于总分片数量的情况
        }
        if (shardInfo.getFailed() > 0) {
            for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
                String reason = failure.reason();//处理潜在的失败
            }
        }

        //还可以检查文档是否被找到：
        if (deleteResponse.getResult() == DocWriteResponse.Result.NOT_FOUND) {
            //如果找不到要删除的文档，执行某些操作
        }
    }

}
