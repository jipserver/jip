package jip.utils;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String to time mapper. The following formats are accepted:
 *
 * <pre>
 *    minutes
 *    minutes:seconds
 *    hours:minutes:seconds
 *    days-hours
 *    days-hours:minutes
 *    days-hours:minutes:seconds
 * </pre>
 *
 * NOTE that this currently stores time in minutes, so minimal amount is one minute
 * and seconds are rounded.
 *
 */
public class Time implements Serializable, Comparable<Time>{
    private static final long serialVersionUID = 8362937739843428652L;
    private static final int MINUTES_PER_DAY = 24 * 60;
    private static Pattern minutes = Pattern.compile("^(\\d+)$");
    private static Pattern minutes_seconds = Pattern.compile("^(\\d+):(\\d+)$");
    private static Pattern hours_minutes_seconds = Pattern.compile("^(\\d+):(\\d+):(\\d+)$");
    private static Pattern days_hours = Pattern.compile("^(\\d+)-(\\d+)$");
    private static Pattern days_hours_minutes = Pattern.compile("^(\\d+)-(\\d+):(\\d+)$");
    private static Pattern days_hours_minutes_seconds = Pattern.compile("^(\\d+)-(\\d+):(\\d+):(\\d+)$");


    /**
     * The time in seconds
     */
    private long time;

    /**
     * Default empty constructor
     */
    public Time() {
    }

    /**
     * Create a new time instance
     *
     * @param s the time format
     */
    public Time(String s) {
        // parse minutes:seconds
        try {
            Matcher m = minutes.matcher(s);
            if (m.matches()) {
                time =  Long.parseLong(m.group(0))*60;
            } else if ((m = minutes_seconds.matcher(s)).matches()) {
                long minutes = Long.parseLong(m.group(1));
                long seconds = Long.parseLong(m.group(2));
                time =  (minutes*60) + seconds;
            } else if ((m = hours_minutes_seconds.matcher(s)).matches()) {
                long hours = Long.parseLong(m.group(1));
                long minutes = Long.parseLong(m.group(2));
                long seconds = Long.parseLong(m.group(3));
                time = ((hours * 60) + minutes)*60 + seconds;
            } else if ((m = days_hours.matcher(s)).matches()) {
                long days = Long.parseLong(m.group(1));
                long hours = Long.parseLong(m.group(2));
                time =  ((days * (24 * 60)) + (hours * 60))*60;
            } else if ((m = days_hours_minutes.matcher(s)).matches()) {
                long days = Long.parseLong(m.group(1));
                long hours = Long.parseLong(m.group(2));
                long minutes = Long.parseLong(m.group(3));
                time =  ((days * (24 * 60)) + (hours * 60) + minutes)*60;
            } else if ((m = days_hours_minutes_seconds.matcher(s)).matches()) {
                long days = Long.parseLong(m.group(1));
                long hours = Long.parseLong(m.group(2));
                long minutes = Long.parseLong(m.group(3));
                long seconds = Long.parseLong(m.group(4));
                time =  ((days * (24 * 60)) + (hours * 60) + minutes)*60 + seconds;
            }else{
                throw new IllegalArgumentException("Unknown time format for " + s);
            }

            if(time < 0){
                time = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unknown time format for " + s);
        }
    }

    /**
     * Create time and initialize with given seconds
     *
     * @param time time in seconds
     */
    public Time(long time) {
        this.time= time;
    }

    /**
     * Get the time in seconds
     *
     * @return time time in seconds
     */
    public long getTime() {
        return time;
    }

    /**
     * Set the time in seconds
     * @param time time in seconds
     */
    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        long time = this.time;
        long seconds = time%60;
        time = time/60;
        long days = (long) (Math.floor(time / MINUTES_PER_DAY));
        long hours = (long) Math.floor(time / 60.0);
        long minutes = (long) (Math.ceil(time % 60.0));
        if(days > 0){
            hours = Math.max(0, hours-(days*24));
            return String.format("%d-%02d:%02d:%02d", days, hours, minutes,seconds);
        }else{
            return String.format("%02d:%02d:%02d", hours, minutes,seconds);
        }
    }

    @Override
    public int compareTo(Time o) {
        return (time<o.time ? -1 : (time==o.time ? 0 : 1));
    }
}
