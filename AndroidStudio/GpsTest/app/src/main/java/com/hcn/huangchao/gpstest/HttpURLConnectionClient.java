package com.hcn.huangchao.gpstest;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class HttpURLConnectionClient {
    private static final String TAG = "HttpURLConnectionClient";
    private final String POST="POST";
    private final String GET="GET";
    private final String UTF_8 = "UTF-8";

    private static HttpURLConnectionClient httpURLConnectionClient = null;

    private HttpURLConnectionClient(){}


    public static HttpURLConnectionClient getInstance() {
        if(httpURLConnectionClient == null) {
            httpURLConnectionClient = new HttpURLConnectionClient();
        }
        return httpURLConnectionClient;
    }

    public String get(String requestUrl) {
        StringBuffer buffer = new StringBuffer();
        BufferedReader bufferedReader = null;
        OutputStream outputStream = null;
        HttpURLConnection httpUrlConn = null;

        try {
            httpUrlConn = createHttpURLConnection(requestUrl);
            httpUrlConn.setRequestMethod(GET);
            // 将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            return buffer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            close(bufferedReader,outputStream,httpUrlConn);
        }
    }


    public String post(String requestUrl, Map<String,String> headers, String data) {
        StringBuffer buffer = new StringBuffer();
        BufferedReader bufferedReader = null;
        OutputStream outputStream = null;
        HttpURLConnection httpUrlConn = null;

        try {
            httpUrlConn = createHttpURLConnection(requestUrl);
            httpUrlConn.setRequestMethod(POST);
            addHeaders(httpUrlConn,headers);
            // 当有数据需要提交时
            if (null != data) {
                outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码
                outputStream.write(data.getBytes("UTF-8"));
                outputStream.flush();
            }

            // 将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            return buffer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            close(bufferedReader,outputStream,httpUrlConn);
        }
    }

    public String httpsGet(String requestUrl) {
        StringBuffer buffer = new StringBuffer();

        BufferedReader bufferedReader = null;
        OutputStream outputStream = null;
        HttpsURLConnection httpUrlConn = null;

        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = { new NonAuthenticationX509TrustManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();

            httpUrlConn = (HttpsURLConnection) createHttpURLConnection(requestUrl);
            httpUrlConn.setSSLSocketFactory(ssf);
            httpUrlConn.setRequestMethod(GET);
            httpUrlConn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            if(httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // 将返回的输入流转换成字符串
                InputStream inputStream = httpUrlConn.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, UTF_8);
                bufferedReader = new BufferedReader(inputStreamReader);

                String str = null;
                while ((str = bufferedReader.readLine()) != null) {
                    buffer.append(str);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            close(bufferedReader,outputStream,httpUrlConn);
        }
        return buffer.toString();
    }

    public String httpsPost(String requestUrl, Map<String,String> headers,String data) {
        StringBuffer buffer = new StringBuffer();

        BufferedReader bufferedReader = null;
        OutputStream outputStream = null;
        HttpsURLConnection httpUrlConn = null;

        try {
            // 创建SSLContext对象，并使用我们指定的信任管理器初始化
            TrustManager[] tm = { new NonAuthenticationX509TrustManager() };
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, tm, new java.security.SecureRandom());
            // 从上述SSLContext对象中得到SSLSocketFactory对象
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            //ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); // 允许所有主机的验证

            httpUrlConn = (HttpsURLConnection) createHttpURLConnection(requestUrl);
            httpUrlConn.setSSLSocketFactory(ssf);
            httpUrlConn.setRequestMethod(POST);
            addHeaders(httpUrlConn, headers);
            httpUrlConn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            // 当有数据需要提交时
            if (null != data) {
                outputStream = httpUrlConn.getOutputStream();
                // 注意编码格式，防止中文乱码
                outputStream.write(data.getBytes(UTF_8));
                outputStream.flush();
            }

            if(httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // 将返回的输入流转换成字符串
                InputStream inputStream = httpUrlConn.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, UTF_8);
                bufferedReader = new BufferedReader(inputStreamReader);

                String str = null;
                while ((str = bufferedReader.readLine()) != null) {
                    buffer.append(str);
                }
                //return buffer.toString();
            }
        } catch (Exception e) {
            //throw new RuntimeException(e);
            e.printStackTrace();
        }finally {
            close(bufferedReader,outputStream,httpUrlConn);
        }
        return buffer.toString();
    }

    /* private methods */
    private void addHeaders(HttpURLConnection httpURLConnection,Map<String,String> headers){
        if(headers==null){
            return;
        }
        for(Map.Entry<String,String> header:headers.entrySet()){
            String key = header.getKey();
            String value = header.getValue();
            Log.v(TAG, "k=>v : " + key + "," + value);
            httpURLConnection.addRequestProperty(key, value);
        }
    }


    private HttpURLConnection createHttpURLConnection(String requestUrl){
        URL url = null;
        HttpURLConnection httpUrlConn = null;
        try {
            url = new URL(requestUrl);
            httpUrlConn = (HttpURLConnection) url.openConnection();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        httpUrlConn.setRequestProperty("Charset", "UTF-8");
        httpUrlConn.setDoOutput(true);
        httpUrlConn.setDoInput(true);
        //httpUrlConn.setUseCaches(false);
        return httpUrlConn;
    }

    private void close(BufferedReader bufferedReader,
                       OutputStream outputStream,
                       HttpURLConnection httpUrlConn) {
        if(outputStream!=null){
            try{
                outputStream.close();
            }catch(Throwable e){
                e.printStackTrace();
            }
        }
        if(bufferedReader!=null){
            try{
                bufferedReader.close();
            }catch(Throwable e){
                e.printStackTrace();
            }
        }
        if(httpUrlConn!=null){
            try{
                httpUrlConn.disconnect();
            }catch(Throwable e){
                e.printStackTrace();
            }
        }
    }
}
