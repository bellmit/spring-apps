package com.haozhuo.datag.service;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetUserInfo {
    @Autowired
    private EsService esService;

    public List getUserInfo(String rptid, String sex, String age){
        String label = esService.getLabelsByReportId(rptid);
        String s = "";
        List list = new ArrayList();
        if (sex.equals("女")&&Integer.parseInt(age)>18&&Integer.parseInt(age)<60){
            if (label.contains("白带清洁度")||label.contains("类风湿因子")||label.contains("宫颈")){
                list.add(rptid);
            }
        }
        return list;
    }

    public void test() throws IOException {
        String pathname = "D:\\workspace\\spring-apps\\datag\\src\\main\\excel\\sexRptId.txt";
        FileReader reader = null;
        List s1 = new ArrayList();
        try {
            reader = new FileReader(pathname);
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                String[] s = line.split(",");
                String label = esService.getLabelsByReportId("45535974");
                List list = new ArrayList();
                if (s[0].equals("女")&&Integer.parseInt(s[3])>18&&Integer.parseInt(s[3])<60){
                    if (label.contains("白带清洁度")||label.contains("类风湿因子")||label.contains("宫颈")){
                        System.out.println(line);
                    }
                }

            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
