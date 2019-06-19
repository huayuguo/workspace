package com.hcn.huangchao.remotetest;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpUtils {
    private static final String TAG = "HttpUtils";
    private static String firstCookie;
    private static HttpUtils httpUtils = null;

    private HttpUtils() {
        firstCookie = null;
    }

    public static HttpUtils getInstance() {
        if(httpUtils == null) {
            httpUtils = new HttpUtils();
        }
        return httpUtils;
    }

    public String httpPost(String url_s, String param) {
        String result = new String();
        HttpURLConnection connection = null;
        try {
            URL url = new URL(url_s);
            connection = (HttpURLConnection) url.openConnection();
            // 设置请求方式
            connection.setRequestMethod("POST");
            // 设置编码格式
            connection.setRequestProperty("Charset", "UTF-8");
            // 设置cookie
            if(firstCookie != null) {
                connection.setRequestProperty("Cookie", firstCookie);
            }
            // 设置容许输出
            connection.setDoOutput(true);
            connection.setDoInput(true);

            // 上传
            if(param != null) {
                OutputStream os = connection.getOutputStream();
                os.write(param.getBytes());
                os.flush();
                os.close();
            }

            // 获取返回数据
            if(connection.getResponseCode() == 200){
                InputStream is = connection.getInputStream();
                StringBuilder sb = new StringBuilder();
                String line;

                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                result = sb.toString();
                Log.e(TAG, ">>>" + result);

                //获取cookie
                Map<String,List<String>> map = connection.getHeaderFields();
                Set<String> set = map.keySet();
                for (Iterator iterator = set.iterator(); iterator.hasNext();) {
                    String key = (String) iterator.next();
                    if (key != null && key.equals("Set-Cookie")) {
                        List<String> list = map.get(key);
                        StringBuilder builder = new StringBuilder();
                        for (String str : list) {
                            builder.append(str).toString();
                        }
                        firstCookie = builder.toString();
                        Log.i(TAG, firstCookie);
                    }
                }
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if(connection!=null){
                connection.disconnect();
            }
        }
        return result;
    }
}
