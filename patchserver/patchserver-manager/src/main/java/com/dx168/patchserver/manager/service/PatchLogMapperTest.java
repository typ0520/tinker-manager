package com.dx168.patchserver.manager.service;

import com.alibaba.fastjson.JSON;
import com.dx168.patchserver.core.domain.PatchLog;
import com.dx168.patchserver.manager.application.ManagerApplication;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by guoyongping on 2017/11/1.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManagerApplication.class)
public class PatchLogMapperTest {

    private Logger logger = LoggerFactory.getLogger(PatchLogMapperTest.class);

    @Autowired
    private PatchLogService patchLogService;


    @Test
    public void testFindByPage(){

        Map param = new HashMap();
        param.put("appUid", "20170912180443527-2444");
        param.put("patchVersion", 1);

        Page<PatchLog> page = patchLogService.findByPage(param,1,5);
        PageInfo<PatchLog> pageInfo = new PageInfo<>(page);
        Assert.assertNotNull(page);
        logger.info(pageInfo.toString());
        logger.info(JSON.toJSONString(pageInfo));

    }
}
