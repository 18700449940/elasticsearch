package com.sang.elasticsearch.util;

import com.sang.elasticsearch.conf.ESClientSpringFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;


public class EsUtil {
    // 对象池配置类，不写也可以，采用默认配置
    private static GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();

    // 采用默认配置maxTotal是8，池中有8个client
    {
        poolConfig.setMaxTotal(8);
    }

    // 要池化的对象的工厂类，这个是我们要实现的类
    private static ESClientSpringFactory esClientPoolFactory = new ESClientSpringFactory();
    // 利用对象工厂类和配置类生成对象池
    private static GenericObjectPool<RestHighLevelClient> clientPool = new GenericObjectPool<>(esClientPoolFactory,
            poolConfig);

    /**
     * 获得对象
     *
     * @return
     * @throws Exception
     */
    private static RestHighLevelClient getClient() throws Exception {
        // 从池中取一个对象
        RestHighLevelClient client = clientPool.borrowObject();
        return client;
    }

    /**
     * 归还对象
     *
     * @param client
     */
    private static void close(RestHighLevelClient client) {
        // 使用完毕之后，归还对象
        clientPool.returnObject(client);
    }

    public static IndexResponse index(IndexRequest request) throws Exception {
        RestHighLevelClient client = getClient();

        IndexResponse response = null;
        try {
            response = client.index(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        close(client);
        return response;
    }

    public static SearchResponse search(SearchRequest request) throws Exception {
        RestHighLevelClient client = getClient();
        SearchResponse response = null;
        try {
            response = client.search(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        close(client);
        return response;
    }

    public static DeleteResponse delete(DeleteRequest request) throws Exception {
        RestHighLevelClient client = getClient();
        DeleteResponse response = null;
        try {
            response = client.delete(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        close(client);
        return response;
    }

    public static GetResponse get(GetRequest request) throws Exception {
        RestHighLevelClient client = getClient();
        GetResponse response = null;
        try {
            response = client.get(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        close(client);
        return response;
    }
    public static CreateIndexResponse create(CreateIndexRequest request) throws Exception {
        RestHighLevelClient client = getClient();
        CreateIndexResponse response = null;
        try {
            response = client.indices().create(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        close(client);
        return response;
    }

    public static boolean exists(GetIndexRequest request) throws Exception {
        RestHighLevelClient client = getClient();
        CreateIndexResponse response = null;
        boolean exists=false;
        try {
             exists = client.indices().exists(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        close(client);
        return exists;
    }
}
