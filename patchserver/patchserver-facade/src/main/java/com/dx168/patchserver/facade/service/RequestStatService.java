package com.dx168.patchserver.facade.service;

import com.dx168.patchserver.facade.dto.StatInfo;
import com.dx168.patchserver.facade.web.ApiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tong on 16/11/16.
 */
@Service
public class RequestStatService {
    private static final Logger LOG = LoggerFactory.getLogger(ApiController.class);

    //当天总的请求次数
    private final AtomicInteger todayViews = new AtomicInteger(0);
    //一分钟的请求次数
    private final AtomicInteger minuteViews = new AtomicInteger(0);
    //当天并发量最大的每分钟请求量
    private final AtomicInteger minuteMaxViews = new AtomicInteger(0);

    public void increment() {
        todayViews.getAndIncrement();
        minuteViews.getAndIncrement();
    }

    public StatInfo getStatInfo() {
        return new StatInfo(todayViews.get(),minuteViews.get(),minuteMaxViews.get());
    }

    //每分钟执行一次
    @Scheduled(cron="0 */1 * * * ?")
    public void stat_minute() {
        int minuteViewsCount = minuteViews.get();
        int minuteMaxViewsCount = minuteMaxViews.get();

        if (minuteViewsCount > minuteMaxViewsCount) {
            minuteMaxViews.set(minuteViewsCount);
        }
        minuteViews.set(0);

        LOG.info("stat_minute " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) +  " ,minuteViewsCount: " + minuteViewsCount + " ,minuteMaxViewsCount: " + minuteMaxViewsCount);
    }

    //每天晚上0点执行一次
    @Scheduled(cron="0 0 00 * * ?")
    public void stat_day() {
        todayViews.set(0);
        minuteMaxViews.set(0);
        minuteMaxViews.set(0);
    }
}
