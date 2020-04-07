package com.winsun.iot.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class HttpClientUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    public static String doGet(String url, Map<String, Object> params, String username, String password) {
        String apiUrl = url;
        StringBuffer param = new StringBuffer();
        int i = 0;
        for (String key : params.keySet()) {
            if (i == 0)
                param.append("?");
            else
                param.append("&");
            param.append(key).append("=").append(params.get(key));
            i++;
        }
        apiUrl += param;
        String result = null;
        CloseableHttpClient httpclient = getHttpClient(username,password);
        try {
            logger.info(apiUrl);
            HttpGet httpPost = new HttpGet(apiUrl);
            HttpResponse response = httpclient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            logger.info("执行状态码 : " + statusCode);

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                result = EntityUtils.toString(entity, "UTF-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static CloseableHttpClient getHttpClient(String username, String password){
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        provider.setCredentials(AuthScope.ANY, credentials);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(provider)
                .build();
        return  httpClient;
    }
}
