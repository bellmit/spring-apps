package com.haozhuo.datagservice.web;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对应原来
 */
@RestController
@RequestMapping(value = "/service/provider")
public class ProviderController {

//    /**
//     *旧的接口：
//     * 对应developer-api项目中的GetSimiIdController的getSimiIdNew()方法
//     * 生产环境:
//     * http://47.98.165.120:8085/getSimi/all?informationId=123273
//     * 测试环境:
//     * http://192.168.1.152:8085/getSimi/all?informationId=56
//     */
//    @ApiOperation(value = "获取相似的资讯、视频、直播", notes = "输入information_id,返回相似的id和相似度")
//    @RequestMapping(value = "/getSimi/all", method = RequestMethod.GET)
//    public Object  getSimiAll(@RequestParam(value = "informationId", defaultValue = "111") String informationId) {
//        return null;
//    }
}
