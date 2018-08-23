package com.haozhuo.bisys.web;

import com.haozhuo.bisys.service.JdbcService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Lucius on 8/23/18.
 */
@RequestMapping(value = "/")
@RestController
public class BisysController {
    @Autowired
    private JdbcService jdbcService;

    @GetMapping("/test/{channelId}")
    @ApiOperation(value = "", notes = "")
    public Object getChannelName(@PathVariable(value="channelId") int channelId) {
        return jdbcService.getChannelInfo(channelId);
    }
}
