package com.haozhuo.datag.web;

import com.haozhuo.datag.service.BisysJdbcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Lucius on 9/3/18.
 */
@RequestMapping(value = "/bisys")
@RestController
public class BisysController {
    @Autowired
    private BisysJdbcService bisysJdbcService;
}
