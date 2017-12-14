package com.hty.util.filesync.util;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 任务下载进度打印工具类
 */
public class ProgressPrinter {
    /**
     * 上一秒下载的字节数
     */
    private long lastSecReadBytes;
    /**
     * 下载总字节数
     */
    private long totalReadBytes;
    /**
     * 文件名称
     */
    private String filename;
    /**
     * 文件大小
     */
    private long  fileLen;

    private String prefix;

    private int shutdownCount = 0;
    /**
     * 定时器
     */
    Timer timer = new Timer();

    public ProgressPrinter(String filename, long  fileLen, String prefix) {
        this.prefix = prefix;
        this.filename = filename;
        this.fileLen = fileLen;
        timer.scheduleAtFixedRate(new TimerTask1(), 0, 1000);
    }

    public void updateTotalBytes(long len) {
        totalReadBytes+=len;
        lastSecReadBytes+=len;
        //如果连续30s速度低于200k/s，则抛出异常，重新下载
        if(shutdownCount >= 30) {
            throw new RuntimeException("当前下载速度过低，将重置任务");
        }
    }

    private void resetLastSecBytes() {
        lastSecReadBytes = 0;
    }

    public void cancelTimer() {
        timer.cancel();
        System.out.println("\n");
    }

    private class TimerTask1 extends TimerTask {

        private DecimalFormat bd = new DecimalFormat("0.00");

        private String human_readable_filesize(long size) {
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

        @Override
        public void run() {
            System.out.print(prefix + "正在下载: " + filename +
                " | 已下载:" + human_readable_filesize(totalReadBytes) +
                " | 平均速度:" + human_readable_filesize(lastSecReadBytes) + "/s\r");
            if(lastSecReadBytes < 204800) {
                shutdownCount++;
            } else {
                shutdownCount = 0;
            }
            resetLastSecBytes();
        }
    }
}
