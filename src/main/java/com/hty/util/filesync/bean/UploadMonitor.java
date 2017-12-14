package com.hty.util.filesync.bean;

import com.jcraft.jsch.SftpProgressMonitor;

import java.text.DecimalFormat;

public class UploadMonitor implements SftpProgressMonitor {

    private long uploadedBytes = 0L;

    private long lastUploadBytes = 0L;

    private String fileName;
    private long fileSize;
    private long firstTimestamp;
    private long lastTimestamp;

    public UploadMonitor() {
    }

    public UploadMonitor(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    @Override
    public void init(int op, String src, String dest, long max) {
        System.out.println("Uploading: " + fileName + " --> " + dest);
        lastTimestamp = System.currentTimeMillis();
        firstTimestamp = lastTimestamp;
    }

    @Override
    public boolean count(long l) {
        uploadedBytes += l;
        lastUploadBytes += l;
        long curTimestamp = System.currentTimeMillis();
        if(curTimestamp - lastTimestamp >= 1000 || (uploadedBytes == fileSize)) {
            System.out.print("Total: " + human_readable_filesize(fileSize) + " | " +
                    "Uploaded: " + human_readable_filesize(uploadedBytes) + " | " +
                    "Speed["+ human_readable_filesize((long) (lastUploadBytes * 1000 * 1.0 / (curTimestamp - lastTimestamp))) +"/s] | " +
                    "AvgSpeed["+ human_readable_filesize((long) (uploadedBytes * 1000 * 1.0 / (curTimestamp - firstTimestamp))) +"/s] | " +
                    "Percent: " + printPercent() + (uploadedBytes == fileSize ? " - Done!\n" : "\r"));
            lastTimestamp = curTimestamp;
            lastUploadBytes = 0;
        }
        return true;
    }


    @Override
    public void end() {
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
        if(uploadedBytes == fileSize || fileSize == 0)
            return "100%";
        else {
            DecimalFormat bd = new DecimalFormat("0.00");
            return bd.format((uploadedBytes) * 100.0 / fileSize) + "%";
        }
    }


}
