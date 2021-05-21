package com.haozhuo.datag.service;


import com.alibaba.fastjson.JSONObject;
import com.haozhuo.datag.model.ReturnCodeAndMsgEnum;
import com.haozhuo.datag.model.ReturnValue;
import com.haozhuo.datag.util.CloseableHttpClientToInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    private final KafkaService kafkaService;
    private final static String stdtUrl = "http://192.168.20.159/api/hm/getStdRpt?id=";

    public FileService(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    public void getStdReport(String rptid){

        String s  = CloseableHttpClientToInterface.doGet(stdtUrl+rptid);
        String array =   JSONObject.parseObject(s).get("value").toString();
        kafkaService.sendStdReportMsg(array);
        try {
            Thread.sleep(1);   // 休眠10ms
        } catch (Exception e) {
            System.out.println("Got an exception!");
        }

    }

    public ReturnValue uploadFileTest(MultipartFile zipFile) throws IOException {
        String targetFilePath = "D:\\test\\uploadTest";
        String fileName = UUID.randomUUID().toString().replace("-", "");

        String fileSuffix = getFileSuffix(zipFile);
        if (fileSuffix != null) { // 拼接后缀
            fileName += fileSuffix;
        }
        System.out.println(fileName);
        File targetFile = new File(targetFilePath + File.separator + fileName);

        //File targetFile = new File(targetFilePath + File.separator + fileName);
        List<String> rptList = upload(zipFile);
        for(String rptId:rptList){
            getStdReport(rptId);
        }

        /*FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(targetFile);
            IOUtils.copy(zipFile.getInputStream(), fileOutputStream);
            logger.info("------>>>>>>uploaded a file successfully!<<<<<<------");
        } catch (IOException e) {
            return new ReturnValue<>(-1, null);
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                logger.error("", e);
            }
        }*/
        return new ReturnValue<>(ReturnCodeAndMsgEnum.Success, "完成");
    }

    private String getFileSuffix(MultipartFile file) {
        if (file == null) {
            return null;
        }
        String fileName = file.getOriginalFilename();
        System.out.println("原始名称：" + fileName);
        int suffixIndex = fileName.lastIndexOf(".");
        if (suffixIndex == -1) { // 无后缀
            return null;
        } else {   // 存在后缀
            return fileName.substring(suffixIndex, fileName.length());
        }
    }

    public List<String> upload(MultipartFile file) throws IOException {
        ArrayList<String> list = new ArrayList();
        //起手转成字符流
        InputStream is = file.getInputStream();
        InputStreamReader isReader = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isReader);
        //循环逐行读取
        while (br.ready()) {
            list.add(br.readLine().toString());
        }
        //关闭流，讲究
        br.close();
        System.out.println(list.size());
        return  list;

    }

}