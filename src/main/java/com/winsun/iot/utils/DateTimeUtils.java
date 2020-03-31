package com.winsun.iot.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtils {

    private static LocalTime dayStartTime = LocalTime.of(0, 0);

    private static DateTimeFormatter fullTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static DateTimeFormatter fullSecondFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static DateTimeFormatter fullTimeStrFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static DateTimeFormatter datePlainFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static DateTimeFormatter monthPlainFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");

    private static DateTimeFormatter minuteFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private static DateTimeFormatter sencondFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static DateTimeFormatter fullTimeHour = DateTimeFormatter.ofPattern("yyyyMMddHH");

    private static DateTimeFormatter fullMinutes = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:00");

    /**
     * 计算周期数
     *
     * @param startTime
     * @param period
     * @return
     */
    public static int getPeriodIndex(LocalDateTime startTime, int period) {

        Duration duration = Duration.between(dayStartTime, startTime.toLocalTime());

        return (int) duration.toMinutes() / period;
    }

    public static String formatTime(LocalDateTime dateTime) {
        return dateTime.format(timeFormatter);
    }

    /**
     * yyyy-MM-dd
     *
     * @param dateTime
     * @return
     */
    public static String formatDate(LocalDateTime dateTime) {
        return dateTime.format(dateFormatter);
    }

    /**
     * yyyy-MM-dd
     *
     * @param dateTime
     * @return
     */
    public static String formatDate(LocalDate dateTime) {
        return dateTime.format(dateFormatter);
    }

    /**
     * yyyyMMdd
     *
     * @param dateTime
     * @return
     */
    public static String formatDatePlain(LocalDateTime dateTime) {
        return dateTime.format(datePlainFormatter);
    }

    public static String formatFull(LocalDateTime now) {
        return now.format(fullTimeFormatter);
    }

    public static String formatFullSecond(LocalDateTime now) {
        return now.format(fullSecondFormatter);
    }

    public static String formatTimeHour(LocalDateTime now) {
        return now.format(fullTimeHour);
    }


    public static String formatFullStr(LocalDateTime now) {
        return now.format(fullTimeStrFormatter);
    }


    public static LocalDate parseDate(String date) {
        return LocalDate.parse(date, dateFormatter);
    }

    public static LocalDate parseDatePlain(String date) {
        return LocalDate.parse(date, datePlainFormatter);
    }

    public static LocalDateTime parseDateHourPlain(String date) {
        return LocalDateTime.parse(date, fullTimeHour);
    }

    public static LocalTime parseTime(String time) {
        return LocalTime.parse(time, timeFormatter);
    }

    public static LocalTime parseSecondTime(String time) {
        return LocalTime.parse(time, sencondFormatter);
    }

    /**
     * @param time HH:mm
     * @return
     */
    public static LocalTime parseMinuteTime(String time) {
        return LocalTime.parse(time, minuteFormatter);
    }

    public static String formatMinute(LocalDateTime time) {
        return time.format(fullMinutes);
    }
    public static String formatSecond(LocalDateTime time) {
        return time.format(fullSecondFormatter);
    }

    /**
     * @param fullTimeStr yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static LocalDateTime parseFullMinutes(String fullTimeStr) {
        return LocalDateTime.parse(fullTimeStr, fullMinutes);
    }

    /**
     * @param fullTimeStr yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static LocalDateTime parseFullSecond(String fullTimeStr) {
        return LocalDateTime.parse(fullTimeStr, fullSecondFormatter);
    }

    public static String getDuration(LocalDateTime startTime, LocalDateTime endTime) {
        return DateTimeUtils.formatTime(startTime) + DateTimeUtils.formatTime(endTime);
    }

    public static String getDuration(LocalDateTime startTime, int period) {
        return DateTimeUtils.formatTime(startTime) + DateTimeUtils.formatTime(startTime.plusMinutes(period));
    }

    private static DateTimeFormatter dateTimePlain = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    /**
     * @param timeStr yyyyMMddHHmm
     * @return
     */
    public static LocalDateTime parseTimePlain(String timeStr) {
        return LocalDateTime.parse(timeStr, dateTimePlain);
    }

    private static DateTimeFormatter monthPlain = DateTimeFormatter.ofPattern("yyyyMM");

    public static String formatMonth(LocalDateTime endTime) {
        return endTime.format(monthPlain);
    }

    private static DateTimeFormatter monthInfo = DateTimeFormatter.ofPattern("yyyy-MM");

    public static String formatMonthInfo(LocalDateTime endTime) {
        return endTime.format(monthInfo);
    }

    public static String dateToISODate(LocalDateTime dateTime) {
        //T代表后面跟着时间，Z代表UTC统一时间

        return null;
    }

    // 01. java.util.Date --> java.time.LocalDateTime
    public static LocalDateTime UDateToLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    // 02. java.util.Date --> java.time.LocalDate
    public static LocalDate UDateToLocalDate(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        return localDateTime.toLocalDate();
    }

    // 03. java.util.Date --> java.time.LocalTime
    public static LocalTime UDateToLocalTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        return localDateTime.toLocalTime();
    }


    // 04. java.time.LocalDateTime --> java.util.Date
    public static Date LocalDateTimeToUdate(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }


    // 05. java.time.LocalDate --> java.util.Date
    public static Date LocalDateToUdate(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
        return Date.from(instant);
    }

    // 06. java.time.LocalTime --> java.util.Date
    public static Date LocalTimeToUdate(LocalTime localTime) {
        LocalDate localDate = LocalDate.now();
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }

    // Obtains an instance of Date from an Instant object.
    public static Date from(Instant instant) {
        try {
            return new Date(instant.toEpochMilli());
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static boolean isBeforeOrEq(LocalDateTime forCheck, LocalDateTime check) {
        return !forCheck.isAfter(check);
    }

    public static boolean isAfterOrEq(LocalDateTime forCheck, LocalDateTime check) {
        return !forCheck.isBefore(check);
    }

    public static boolean isBeforeOrEq(LocalDate forCheck, LocalDate check) {
        return !forCheck.isAfter(check);
    }

    public static boolean isBefore(LocalDate forCheck, LocalDate check) {
        return forCheck.isBefore(check);
    }

    public static boolean isAfterOrEq(LocalDate forCheck, LocalDate check) {
        return !forCheck.isBefore(check);
    }


    public static LocalDateTime parseDateDayPlain(String date) {
        return LocalDateTime.of(LocalDate.parse(date, datePlainFormatter), LocalTime.of(0, 0));
    }

    public static LocalDateTime parseDateMonthPlain(String date) {
        return LocalDateTime.of(LocalDate.parse(date + "01", datePlainFormatter), LocalTime.of(0, 0));
    }

    public static void main(String[] args) {
        String dateTime = DateTimeUtils.formatDate(
                DateTimeUtils.parseDateDayPlain("20200225"));
        System.out.println(dateTime);

        String value = DateTimeUtils.formatMonthInfo(
                DateTimeUtils.parseDateMonthPlain("202002"));

        System.out.println(value);
    }


}
