package com.haozhuo.datag.web;

import com.haozhuo.datag.model.crm.UserIdTagsId;
import com.haozhuo.datag.service.DataetlJdbcService;
import com.haozhuo.datag.service.EsService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by Lucius on 10/9/18.
 */
@RequestMapping(value = "/crm")
@RestController
public class CrmController {
    private static final Logger logger = LoggerFactory.getLogger(CrmController.class);
    @Autowired
    private EsService esService;
    @Autowired
    private DataetlJdbcService dataetlJdbcService;

    @GetMapping("/portrait/{userIds}")
    @ApiOperation(value = "根据userIds返回用户画像", notes = "多个userId使用逗号隔开")
    public Object getTagsIdsByUserIds(@PathVariable(value = "userIds") String userIds) {
        long beginTime = System.currentTimeMillis();
        List<UserIdTagsId> result = esService.getPortraitIds(userIds.split(","));
        logger.info("/portrait/{}  cost: {}ms", userIds, System.currentTimeMillis() - beginTime);
        return result;
    }

    @GetMapping("/portrait/tagsMap")
    public Object getPortraitTagsMap(
            @RequestParam(value = "tagType", defaultValue = "1") int tagType
    ) {
        long beginTime = System.currentTimeMillis();
        Map<String, String> result = dataetlJdbcService.getPortraitMap(tagType);
        logger.info("/portrait/tagsMap  cost: {}ms", System.currentTimeMillis() - beginTime);
        return result;
    }

    @GetMapping("/portrait/getUserIdsByPortraitTagIds")
    @ApiOperation(value = "",
            notes = "tagIds的意思:   \n  " +
                    "sex01,jbg02|age01|bgs02,pay01,jbg03 表示：\n  " +
                    "(sex01 and jbg02) or (age01) or (bgs02 and pay01 and jbg03)  ")
    public Object getUserIdsByPortraitTagIds(
            @RequestParam(value = "tagIds") String tagIds,
            @RequestParam(value = "searchAfterUid", defaultValue = "null") String searchAfterUid,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        long beginTime = System.currentTimeMillis();
        List<String> result = esService.getUserIdsByPortraitTagIds(tagIds, searchAfterUid, size);
        logger.info("/portrait/getUserIdsByPortraitTagIds  cost: {}ms", System.currentTimeMillis() - beginTime);
        return result;
    }


    @GetMapping("/portrait/getCountByPortraitTagIds")
    @ApiOperation(value = "",
            notes = "tagIds的意思:   \n  " +
                    "sex01,jbg02|age01|bgs02,pay01,jbg03 表示：\n  " +
                    "(sex01 and jbg02) or (age01) or (bgs02 and pay01 and jbg03)  ")
    public Object getCountByPortraitTagIds(
            @RequestParam(value = "tagIds") String tagIds
    ) {
        long beginTime = System.currentTimeMillis();
        long count = esService.getCountByPortraitTagIds(tagIds);
        logger.info("/portrait/getCountByPortraitTagIds  cost: {}ms", System.currentTimeMillis() - beginTime);
        return count;
    }

}
