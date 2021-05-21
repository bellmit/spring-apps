package com.haozhuo.datag.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.*;

/**
 * @Author: Yang JianQiu
 * @Date: 2019/4/26 11:41
 * Apache封装好的CloseableHttpClient
 * 【参考资料】
 *  https://www.cnblogs.com/siv8/p/6222709.html
 *  https://blog.csdn.net/qq_35860138/article/details/82967727
 */
public class CloseableHttpClientToInterface {

    private static String tokenString = "";
    private static String AUTH_TOKEN_EXPIRED = "AUTH_TOKEN_EXPIRED";
    private static CloseableHttpClient httpClient = null;

    /**
     * 以get方式调用第三方接口
     * @param url
     * @return
     */
    public static String doGet(String url){
        //创建HttpClient对象
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(url);

        try {

            //api_gateway_auth_token自定义header头，用于token验证使用
            //get.addHeader("api_gateway_auth_token", tokenString);
            get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");
            HttpResponse response = httpClient.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                //返回json格式
                String res = EntityUtils.toString (response.getEntity());
                return res;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 以post方式调用第三方接口
     * @param url
     * @param json
     * @return
     */
    public static String doPost(String url, JSONObject json){

        try {
            if (httpClient == null){
                httpClient = HttpClientBuilder.create().build();
            }

            HttpPost post = new HttpPost(url);

            //api_gateway_auth_token自定义header头，用于token验证使用
            post.addHeader("api_gateway_auth_token", tokenString);
            post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.81 Safari/537.36");

            StringEntity s = new StringEntity(json.toString());
            s.setContentEncoding("UTF-8");
            //发送json数据需要设置contentType
            s.setContentType("application/json");
            //设置请求参数
            post.setEntity(s);
            HttpResponse response = httpClient.execute(post);

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                //返回json格式
                String res = EntityUtils.toString(response.getEntity());
                return res;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (httpClient != null){
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }



    public static void main(String[] args) {
       //String s = doGet("http://192.168.1.152:8700/api/getDesc?id=2515208");
       JSONObject param = new JSONObject();
       param.put("text","子宫:子宫未探及（自述已切除）\\r\\n阴道囊肿可能,建议复查\\r\\n左附件、右附件未发现明显异常");
       //String result = doPost("http://192.168.1.152:8233/data_std/v1/xj",param);
       String uri = "http://tcc.taobao.com/cc/json/mobile_tel_segment.htm?tel=" ;
        String fileName="province.txt";
        try
        {
            BufferedWriter out=new BufferedWriter(new FileWriter(fileName));


            String fileName1= "z.txt";
            String line="";
                BufferedReader in=new BufferedReader(new FileReader(fileName1));
                line=in.readLine();
                while (line!=null)
                {
                    String url = uri+line;
                    System.out.println(url);
                    String result = doGet(url);
                    //System.out.println(result+"========"+line);
                    String province = "";
                    JSONObject provinceobj  = JSONObject.parseObject(result.replace("__GetZoneResult_ = ",""));
                    if(!provinceobj.isEmpty()){
                        province = provinceobj.getString("province").toString();
                    }
                    out.write(line+"\t"+province);
                    out.newLine();
                    line=in.readLine();
                }
                in.close();
                out.close();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

}
