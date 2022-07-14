package com.tht.space.config;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

@Configuration
public class RestTemplateConfig {
    private static final Integer READ_TIME_OUT = 6000;
    private static final Integer CONNECT_TIME_OUT = 6000;

    @Bean
    public RestTemplate restTemplate(){
        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient());
        return new RestTemplate(requestFactory);
    }

    @Bean
    public HttpClient httpClient(){
        // 默认证书有效
        SSLConnectionSocketFactory sslConnectionSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        SSLContext sslContext = null;
        try {
            // 信任所有的SSL证书
            sslContext = SSLContextBuilder.create().setProtocol(SSLConnectionSocketFactory.SSL).loadTrustMaterial((x,y) -> true).build();
        }catch (Exception e){
            e.printStackTrace();
        }
        if (sslContext != null){
            sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext);
        }
        // 支持HTTP、HTTPS
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslConnectionSocketFactory)
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(100);
        connectionManager.setValidateAfterInactivity(2000);
        RequestConfig requestConfig = RequestConfig.custom()
                //服务器返回数据的超时时间
                .setSocketTimeout(READ_TIME_OUT)
                //连接服务器的超时时间
                .setConnectTimeout(CONNECT_TIME_OUT)
                //从连接池获取连接的超时时间
                .setConnectionRequestTimeout(1000)
                .build();
        return HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).setConnectionManager(connectionManager).build();
    }
}
