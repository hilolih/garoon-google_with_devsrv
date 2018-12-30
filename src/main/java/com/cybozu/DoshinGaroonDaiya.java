/*
* garoon-google
* Copyright (c) 2015 Cybozu
*
* Licensed under the MIT License
*/
package com.cybozu;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.cybozu.garoon3.schedule.Span;
import com.cybozu.garoon3.schedule.ScheduleModifyEvents;

/*
* DoshinGaroonDaiya
* Garoonに運行WEBのダイヤを登録する
*
*/
public class DoshinGaroonDaiya {

    private List<com.cybozu.garoon3.schedule.Event> GaroonSchedules;
    private String Keyword = "--- From Unkou Web ---";
    private ScheduleModifyEvents ModifyEvents;

    DoshinGaroonDaiya () {
        this.GaroonSchedules = new ArrayList<com.cybozu.garoon3.schedule.Event>();
        this.ModifyEvents = new ScheduleModifyEvents();
    }

    public void add(com.cybozu.garoon3.schedule.Event event) {
        this.GaroonSchedules.add(event);
    }

    /*
     * event内部をみてもし運行WEBから登録されているダイヤだったらArrayListに加える
     * 
     */
    public void add_if_unkoweb_daiya(com.cybozu.garoon3.schedule.Event event) {
        if ( event.getDescription().indexOf(this.Keyword) >= 0 ) {
            this.GaroonSchedules.add(event);
        }
    }

    public List<com.cybozu.garoon3.schedule.Event> getGaroonSchedules() {
        return this.GaroonSchedules;
    }

    /*
     * 指定した日付にすでに登録した運行WEBダイヤがあるかどうか
     * 
     */
    public Boolean existsGaroonSchedules(Date date){
        return this.GaroonSchedules.stream().anyMatch(ev -> {
            return this.compareDate(date, ev);
        });
    }

    /*
     * すでに登録されているGaroonのイベントと差異があるかどうか
     * 
     */
    public Boolean diffGaroonSchedule(Date date, String daiya){
        // 日付が一致するスケジュールを取得
        List<com.cybozu.garoon3.schedule.Event> list = this.getSameDateSchedule(date);

        if ( list.size() != 1 ) {
            System.err.println("[!] 同日に２つ以上の運行WEBダイヤがあります。処理をスキップします: " + date);
            // 同じイベントがあるようにみせて処理をスキップさせる
            return false;
        }

        // 要素数１つのリストに対し、タイトルが一致している場合はtrue、
        // 同じであればfalseを返す
        // System.out.println(list.stream().noneMatch(ev -> { return ev.getDetail().equals(daiya);}));
        return list.stream().noneMatch(ev -> {
            return ev.getDetail().equals(daiya);
        });
    }

    public void addUpdateEvent(Date date, String daiya) {
        // 日付が一致するスケジュールを取得
        List<com.cybozu.garoon3.schedule.Event> list = this.getSameDateSchedule(date);
        // 要素数１つのリストに対し、ダイヤを更新して更新用リストに格納する
        list.forEach( ev -> {
            // this.dump(ev);
            // System.out.println("----------------------------------------------------------------");
            ev.setDetail( daiya );
            if ( daiya.equals("【休】") ) {
                ev.setPlan("休み");
            } else {
                ev.setPlan("当番");
            }
            // List<Span> ss = new ArrayList<Span>();
            // Span s = new Span();
            // LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            // int y = ldt.getYear();
            // int m = ldt.getMonthValue();
            // int d = ldt.getDayOfMonth();
            // Date start = Date.from(ldt.minusHours(9).atZone(ZoneId.systemDefault()).toInstant());
            // Date end = Date.from(ldt.minusHours(9).plusDays(1).minusMinutes(1).atZone(ZoneId.systemDefault()).toInstant());
            // s.setStart( start );
            // s.setEnd( end );
            // ss.add(s);
            // ev.setSpans(ss);
            //
            //this.dump(ev);
            this.ModifyEvents.addModifyEvent( ev );
        });

    }

    public ScheduleModifyEvents getModifyEvents() {
        return this.ModifyEvents;
    }

    /*
     * Eventの日付と運行WEBダイヤの日付を比較
     */
    private Boolean compareDate(Date date, com.cybozu.garoon3.schedule.Event ev) {
        if (date == null) {
            return false;
        }
        Span span = ev.getSpans().get(0);
        if (span == null) {
            return false;
        }
        Date start = span.getStart();
        return date.compareTo( start ) == 0;
    }

    /*
     * 日付が一致するスケジュールを取得
     */
    private List<com.cybozu.garoon3.schedule.Event> getSameDateSchedule(Date date) {
        return this.GaroonSchedules.stream()
           .filter(ev -> { return this.compareDate(date, ev); })
           .collect(Collectors.toList());
    }

    /*
     *
     */
    private void dump(com.cybozu.garoon3.schedule.Event event) {
        Span span = event.getSpans().get(0);
        Date start = span.getStart();
        Date end = span.getEnd();

        System.out.println( "予定ID( Id ）: " + event.getId() );
        System.out.println( "タイトル( title ）: " + event.getDetail() );
        System.out.println( "タグ( plan ）: " + event.getPlan() );
        System.out.println( "メモ( description ）: " + event.getDescription() );
        System.out.println( "タイムゾーン( timezone ）: " + event.getTimezone() );
        System.out.println( "期間( Span ）: " + start + " to " + end );
        System.out.println( "終日( Allday ）: " + event.isAllDay() );
        System.out.println( "開始のみ( StartOnly ）: " + event.isStartOnly() );
    }

}

