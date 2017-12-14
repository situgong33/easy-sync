package com.hty.util.filesync.bean;

import com.hty.util.filesync.client.FileSyncClient;
import com.hty.util.filesync.util.AppConfig;
import com.jcraft.jsch.SftpProgressMonitor;

import java.text.DecimalFormat;

public class DownloadMonitor implements SftpProgressMonitor {

    private long downloadedBytes = 0L;

    private long lastDownloadBytes = 0L;

    private long fileSize;
    private String fileName;
    private long firstTimestamp;
    private long lastTimestamp;
    private int slowSpeedLastTimes = 0;
    private AppConfig config ;

    public DownloadMonitor(String dest) {
        this.fileName = dest;
    }

    @Override
    public void init(int op, String src, String dest, long max) {
        System.out.println("Downloading: " + src + " --> " + fileName);
        lastTimestamp = System.currentTimeMillis();
        firstTimestamp = lastTimestamp;
        this.fileSize = max;
        config = AppConfig.getInstance();
    }

    @Override
    public boolean count(long l) {
        downloadedBytes += l;
        lastDownloadBytes += l;
        long curTimestamp = System.currentTimeMillis();
        if(curTimestamp - lastTimestamp >= 1000) {
            System.out.print("Total:" + fixLength(human_readable_filesize(fileSize), 9, " ") + "|" +
                    "Downloaded:" + fixLength(human_readable_filesize(downloadedBytes), 9, " ") + "|" +
                    "Speed["+ fixLength(human_readable_filesize((long) (lastDownloadBytes * 1000 * 1.0 / (curTimestamp - lastTimestamp))), 9, " ") +"/s]|" +
                    "AvgSpeed["+ fixLength(human_readable_filesize((long) (downloadedBytes * 1000 * 1.0 / (curTimestamp - firstTimestamp))), 9, " ") +"/s]|" +
                    "Percent:" + printPercent() + "\r");
            //速度连续10次小雨200k/s，则断开重连
            if(lastDownloadBytes * 1000 / (curTimestamp - lastTimestamp) < config.getResetMinSpeed()) {
                slowSpeedLastTimes++;
                if(slowSpeedLastTimes  > config.getResetMaxCount()) {
                    FileSyncClient.setException();
                    return false;
                }
            } else {
                slowSpeedLastTimes = 0;
            }
            lastTimestamp = curTimestamp;
            lastDownloadBytes = 0;
        }
        return true;
    }


    @Override
    public void end() {
        long curTimestamp = System.currentTimeMillis();
        System.out.println("Total:" + fixLength(human_readable_filesize(fileSize), 9, " ") + "|" +
                "Downloaded:" + fixLength(human_readable_filesize(downloadedBytes), 9, " ") + "|" +
                "Speed["+ fixLength(human_readable_filesize((long) (lastDownloadBytes * 1000 * 1.0 / (curTimestamp - lastTimestamp))), 9, " ") +"/s]|" +
                "AvgSpeed["+ fixLength(human_readable_filesize((long) (downloadedBytes * 1000 * 1.0 / (curTimestamp - firstTimestamp))), 9, " ") +"/s]|" +
                "Percent:" + printPercent() + (downloadedBytes == fileSize ? " - Done!" : " - Failed!"));
    }



    /////////////////////////////////////


    private static String human_readable_filesize(long size) {
        DecimalFormat bd = new DecimalFormat("0.00");
        if(size < 1024)
            return String.valueOf(size) + "B";
        else if(size < 1048567)
            return bd.format(size*1.0 / 1024) + "KB";
        else if(size < 1073741824)
            return bd.format(size*1.0 / 1048567) + "MB";
        else
            return bd.format(size*1.0 / 1073741824) + "GB";
    }

    private String printPercent() {
        if(downloadedBytes == fileSize || fileSize == 0)
            return "100%";
        else {
            DecimalFormat bd = new DecimalFormat("0.00");
            return bd.format((downloadedBytes) * 100.0 / fileSize) + "%";
        }
    }

    private static String fixLength(String num, int width, String fixChar) {
        int curlen = num.length();
        if (curlen < width) {
            for (int i = curlen; i < width; i++) {
                num = fixChar + num;
            }
        }
        return num;
    }


}
