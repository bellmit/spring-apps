package com.haozhuo.datag.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.haozhuo.datag.service.EsService;
import org.apache.catalina.LifecycleState;
import org.bouncycastle.math.ec.custom.sec.SecP128R1Curve;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.lang.reflect.Array;
import java.rmi.MarshalledObject;
import java.util.*;

public class LlUtil {


    public static void main(String[] args) throws IOException {

        String s = "a bcdef";

        char[] chars = s.toCharArray();

        System.out.println(Arrays.toString(chars));
        //Io();
    }

    public static void Io() throws IOException {
        String rptid = null;
        String pathname = "D:\\workspace\\spring-apps\\datag\\src\\main\\excel\\rs.txt";
        FileReader reader = null;
        File file =new File("D:\\workspace\\spring-apps\\datag\\src\\main\\excel\\ll.txt");
        Writer out =new FileWriter(file);
        Map<String,String> map = new HashMap<>();
        Map<String,String> map1 = new HashMap<>();
        try {
            reader = new FileReader(pathname);
            BufferedReader br = new BufferedReader(reader);
            String line;

            while ((line = br.readLine()) != null) {
                String[] s = line.split("_");
              /*  if (!map.containsKey(s[0])) {
                    map.put(s[0],s[1]+"@@"+s[2]);
                }else {
                    map.put(s[0],map.get(s[0])+"||"+s[1]+"@@"+s[2]);
                }*/
                out.write(s[0]+"_"+s[1]+"\n");
                out.flush();
            }

            Map<String,String> map2 = new HashMap<>();
            //split[8] text_ref split[9] unit split[10] upd_t
            for (String a : map.keySet()){
                String s = map.get(a);
                System.out.println(s);
                    String[] split = s.split("\\|\\|");
                    String[] news = new String[11];
                    if (split.length==11){
                        String as = "";
                       as = split[10];
                       split[10] = split[8];
                       split[8] = as;
                        String s1 = Arrays.toString(split);
                        map2.put(a,s1);
                    }else if (split.length==10){
                        if (s.contains("unit")){
                                String as = "";
                                as = split[8];
                                split[8] = split[9];
                                split[9] = as;
                            String s1 = Arrays.toString(split);
                            map2.put(a,s1);
                        }
                        //s[8]=text-ref s[9] upd_t
                        if (s.contains("text_ref")){
                            String as = "";
                            as = split[8];
                            split[8] = split[9];
                            split[9] = as;
                            //s[9] = text_ref s[10]=null
                            for (int i = 0;i<split.length;i++){
                                news[i]=split[i];
                            }
                            news[10]=news[9];
                            news[9]="";
                            String s1 = Arrays.toString(news);
                            map2.put(a,s1);
                        }
                    }else{
                        map2.put(a,s);
                    }
                    //map.put(a,)
            }

            for (String s : map2.keySet()){

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }



}
