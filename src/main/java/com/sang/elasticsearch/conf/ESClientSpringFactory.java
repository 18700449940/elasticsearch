package com.sang.elasticsearch.conf;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ESClientSpringFactory  implements PooledObjectFactory<RestHighLevelClient> {
    private Logger logger  = LoggerFactory.getLogger(this.getClass());

    @Override
    public void activateObject(PooledObject<RestHighLevelClient> arg0) throws Exception {
        System.out.println("activateObject");

    }

    /**
     * 销毁对象
     */
    @Override
    public void destroyObject(PooledObject<RestHighLevelClient> pooledObject) throws Exception {
        RestHighLevelClient highLevelClient = pooledObject.getObject();
        highLevelClient.close();
    }

    /**
     * 生产对象
     */
//  @SuppressWarnings({ "resource" })
    @Override
    public PooledObject<RestHighLevelClient> makeObject() throws Exception {
//      Settings settings = Settings.builder().put("cluster.name","elasticsearch").build();
        RestHighLevelClient client = null;
        try {
            /*client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"),9300));*/
            client = new RestHighLevelClient(RestClient.builder(
                    new HttpHost("192.168.3.103", 9201, "http")));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new DefaultPooledObject<RestHighLevelClient>(client);
    }

    @Override
    public void passivateObject(PooledObject<RestHighLevelClient> arg0) throws Exception {
        System.out.println("passivateObject");
    }

    @Override
    public boolean validateObject(PooledObject<RestHighLevelClient> arg0) {
        return true;
    }
}
