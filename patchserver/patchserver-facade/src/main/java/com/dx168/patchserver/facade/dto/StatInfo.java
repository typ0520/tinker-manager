package com.dx168.patchserver.facade.dto;

/**
 * Created by tong on 16/11/16.
 */
public class StatInfo {
    private int todayViews;
    private int minuteViews;
    private int minuteMaxViews;

    public StatInfo() {
    }

    public StatInfo(int todayViews, int minuteViews, int minuteMaxViews) {
        this.todayViews = todayViews;
        this.minuteViews = minuteViews;
        this.minuteMaxViews = minuteMaxViews;
    }

    public int getTodayViews() {
        return todayViews;
    }

    public void setTodayViews(int todayViews) {
        this.todayViews = todayViews;
    }

    public int getMinuteViews() {
        return minuteViews;
    }

    public void setMinuteViews(int minuteViews) {
        this.minuteViews = minuteViews;
    }

    public int getMinuteMaxViews() {
        return minuteMaxViews;
    }

    public void setMinuteMaxViews(int minuteMaxViews) {
        this.minuteMaxViews = minuteMaxViews;
    }
}
