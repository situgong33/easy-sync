package com.hty.util.filesync.client;

import com.hty.util.filesync.BreakListener;
import com.hty.util.filesync.bean.DownloadMonitor;
import com.hty.util.filesync.exception.DownloadSlowException;
import com.hty.util.filesync.filter.FilterChain;
import com.hty.util.filesync.util.AppConfig;
import com.hty.util.ftp.FTPConfig;
import com.hty.util.ftp.simple.FTPUtils;
import com.jcraft.jsch.ChannelSftp;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FTPSyncClient implements Runnable, BreakListener {

    private FTPUtils util ;

    private static DownloadSlowException slowException;

    public void setException() {
        System.out.println("\nDownload will reset due to low speed.");
        slowException = new DownloadSlowException();
    }
    @Override
    public void run() {
        System.out.println("Start in ftp mode.");

        AppConfig config = AppConfig.getInstance();
        String host = config.getProperty("host");
        String port = config.getProperty("port");
        String username = config.getProperty("ftp_user");
        String password = config.getProperty("ftp_password");
        String srcDir = fixRegularPath(config.getProperty("src_dir"));
        String destDir = fixRegularPath(config.getProperty("dest_dir"));
        String delete_after_sync = config.getProperty("delete_after_sync");
        String check_interval = config.getProperty("check_interval");
        String handle_exist_file = config.getProperty("handle_exist_file");
        String _breakpoint_resume = config.getProperty("breakpoint_resume");
        boolean deleteAfterSync = "true".equals(delete_after_sync);
        boolean breakpoint_resume = "true".equalsIgnoreCase(_breakpoint_resume);
        Integer checkInterval = 60;
        try {
            checkInterval = Integer.valueOf(check_interval);
        } catch (NumberFormatException e) {
        }

        FTPConfig ftpconfig = new FTPConfig();
        ftpconfig.setUser(username);
        ftpconfig.setPassword(password);
        ftpconfig.setHost(host);
        ftpconfig.setPort(Integer.valueOf(port));

        ftpconfig.setType(FTPConfig.TYPE_FTP);
        ftpconfig.setBuffSize(10240);
        ftpconfig.setDataWaitTimeout(10000);
        ftpconfig.setMode(FTPConfig.PASSIVE_MODE);
        ftpconfig.setConnectTimeout(30000);

        util = new FTPUtils(ftpconfig);
        boolean success = util.connect();
        while(!success) {
            try {
                Thread.sleep(5000);
                success = util.connect();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        File destFolder = new File(destDir);
        if(!destFolder.exists()) {
            destFolder.mkdirs();
        }

        for(;;) {
            try {
                List<String> list = listFiles(srcDir, new ArrayList<String>(), "", true);
                for (String s : list) {
                    if(slowException != null) {
                        throw slowException;
                    }
                    boolean downsuccess = util.download(srcDir + "/" + s, destDir + "/" + s, handle_exist_file, new DownloadMonitor(s, this), breakpoint_resume);
                    if(downsuccess && deleteAfterSync) {
                        boolean delSuccess = util.rm(srcDir + "/" + s);
                        if(!delSuccess) {
                            System.out.println("Delete remote file filed: " + srcDir + "/" + s);
                        }
                    }
                }
            } catch (Exception e) {
                slowException = null;
                util.disconnect();
                success = util.connect();
                while(!success) {
                    try {
                        Thread.sleep(5000);
                        success = util.connect();
                    } catch (InterruptedException e1) { }
                }

            } finally {
                try {
                    Thread.sleep(checkInterval * 1000);
                } catch (InterruptedException e) { }
            }
        }
    }


    private List<String> listFiles(String path, List<String> list, String prefix, boolean root) {
        FTPFile[] children = util.ls(path);
        if(null != children) {
            for (FTPFile file : children) {
                if(file.isDirectory()) {
                    listFiles(path + "/" + file.getName(), list,
                            root ? file.getName() : (prefix + "/" + file.getName()), false);
                } else {
                    String ret = FilterChain.filter(file.getName());
                    if(null != ret) {
                        list.add(root ? ret : prefix + "/" + ret);
                    }
                }
            }
        }
        return list;
    }

    public String fixRegularPath(String input) {
        if(null == input || "".equals(input.trim())) {
            input = "/";
            return input;
        }
        while(input.endsWith("/")) {
            input = input.substring(0, input.length() - 1).trim();
        }
        while(input.startsWith("/")) {
            input = input.substring(1, input.length()).trim();
        }
        input = "/" + input;
        if("".equals(input.trim())) {
            input = "/";
        }
        return input;
    }

}
