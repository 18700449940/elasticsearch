package com.sang.elasticsearch;

import com.sang.elasticsearch.bean.Book;
import com.sang.elasticsearch.bean.Chapter;
import com.sang.elasticsearch.service.ElasticService;
import org.apache.http.*;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GetDataMain {
    private static final String baseUrl = "http://www.jjwxc.net/bookbase.php?fw0=0&fbsj=0&ycx0=0&xx0=0&mainview0=0&sd0=0&lx0=0&fg0=0&sortType=0&isfinish=0&collectiontypes=ors&searchkeywords=&page=";
    /**
     * 全局连接池对象
     */
    private static final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();

    private static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(30, 30,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(20000),new BlockingRejectedExecutionHandler());

    private static final CloseableHttpClient httpClient;
    @Autowired
    private ElasticService elasticService;

    private static AtomicInteger totalCount = new AtomicInteger();

    private static boolean isSaveDB = true;

    /**
     * 静态代码块配置连接池信息
     */
    static {

        // 设置最大连接数
        connManager.setMaxTotal(200);
        // 设置每个连接的路由数
        connManager.setDefaultMaxPerRoute(20);

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        configureHttpClient2(httpClientBuilder);
        httpClient = httpClientBuilder.build();

    }

   /* public static void main(String[] args) throws Exception {
        new GetDataMain().addData();
    }*/

    public void addData() throws Exception {
        totalCount.set(0);
        for (int i = 1; i < 10000; i++) {
            String tmpUrl = baseUrl + i;
            Document doc = get(tmpUrl);
            Elements trs = null;
            try {
                trs = doc.getElementsByTag("table").get(0).getElementsByTag("tr").not(":first-child");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("-----current page-----" + i);
                return;
            }
            if (trs != null) {
                Iterator<Element> iterator = trs.iterator();
                while (iterator.hasNext()) {
                    Element item = iterator.next();
                    String id = UUID.randomUUID().toString().replaceAll("-", "");
                    String author = item.getElementsByTag("a").get(0).text();
                    String bookName = item.getElementsByTag("a").get(1).text();
                    String types = item.getElementsByTag("td").get(2).text();
                    String style = item.getElementsByTag("td").get(3).text();
                    String wordCount = item.getElementsByTag("td").get(5).text();
                    String workScore = item.getElementsByTag("td").get(6).text();
                    String updateTime = item.getElementsByTag("td").get(7).text();
                    Book book = new Book();
                    book.setId(id);
                    book.setName(bookName);
                    book.setAuthor(author);
                    book.setTypes(types.split("-"));
                    book.setStyle(style);
                    book.setWordCount(Integer.parseInt(wordCount));
                    book.setWorkScore(Long.parseLong(workScore));
                    book.setUpdateTime(updateTime);
                    System.out.println(book);
                    if (isSaveDB) {
                        elasticService.add(book);
                    }
                    String url = item.getElementsByTag("a").get(1).attr("href");
                    url = "http://www.jjwxc.net/" + url;
                    System.out.println("now access " + url);
                    getBookByUrl(url,id);
                }
            }
        }
        System.out.println("--totalCount-->" + totalCount);
        httpClient.close();

    }

    private void getBookByUrl(String url,String bookId) throws Exception {
        Document doc = get(url);


        Elements chapters = null;
        try {
            chapters = doc.getElementById("oneboolt").getElementsByTag("tr").select("[itemprop=chapter]:not(:has(font))");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Iterator<Element> iterator = chapters.iterator();
        while (iterator.hasNext()) {
            Element chapter = iterator.next();

              poolExecutor.submit(() -> {
                try {
                    String num = chapter.getElementsByTag("td").get(0).text();
                    String name = chapter.getElementsByTag("td").get(1).getElementsByTag("a").get(0).text();
                    String title = chapter.getElementsByTag("td").get(2).text();
                    int wordCount = Integer.parseInt(chapter.getElementsByTag("td").get(3).text());
                    String updateTime = chapter.getElementsByTag("td").get(5).getElementsByTag("span").text();
                    String href = chapter.getElementsByTag("td").get(1).getElementsByTag("a").get(0).attr("href");
                    Document contentDoc = get(href);
                    String content=contentDoc.getElementsByClass("noveltext").get(0).ownText();

                    Chapter chapterVO=new Chapter();
                    chapterVO.setId(bookId+"-"+num);
                    chapterVO.setName(name);
                    chapterVO.setTitle(title);
                    chapterVO.setWordCount(wordCount);
                    chapterVO.setUpdateTime(updateTime);
                    chapterVO.setContent(content);
                    System.out.println(chapterVO);
                    if (isSaveDB) {
                        elasticService.add(chapterVO);
                    }
                    totalCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static Document get(String url) throws IOException {
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse resp = httpClient.execute(get);
        HttpEntity httpEntity = resp.getEntity();
        Document doc = Jsoup.parse(EntityUtils.toString(httpEntity, "gb2312"));
        EntityUtils.consume(httpEntity);
        resp.close();
        return doc;
    }

    public static void configureHttpClient2(HttpClientBuilder clientBuilder) {
        clientBuilder.setConnectionManager(connManager);
        clientBuilder.addInterceptorFirst(new HttpResponseInterceptor() {
            public void process(final HttpResponse response,
                                final HttpContext context) throws HttpException,
                    IOException {
                HttpEntity entity = response.getEntity();
                Header ceheader = entity.getContentEncoding();
                if (ceheader != null) {
                    HeaderElement[] codecs = ceheader.getElements();
                    for (int i = 0; i < codecs.length; i++) {
                        if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                            response.setEntity(new GzipDecompressingEntity(
                                    response.getEntity()));
                            return;
                        }
                    }
                }
            }

        });
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);

            clientBuilder.setSSLContext(ctx);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 自定义拒绝执行策略：阻塞式地将任务添加至工作队列中
     *
     * @author hasee
     *
     */
    private static class BlockingRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (!executor.isShutdown()) {
                try {
                    // 使用阻塞方法向工作队列中添加任务
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    executor.execute(r);
                }

            }
        }
    }

}
