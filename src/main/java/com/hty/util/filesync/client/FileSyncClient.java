package com.hty.util.filesync.client;

import com.hty.util.filesync.bean.DownloadMonitor;
import com.hty.util.filesync.filter.FilterChain;
import com.hty.util.filesync.util.AppConfig;
import com.hty.util.ftp.FTPConfig;
import com.hty.util.ftp.sftp.SFTPUtils;
import com.jcraft.jsch.ChannelSftp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class FileSyncClient implements Runnable {

    private SFTPUtils util ;
    @Override
    public void run() {

        AppConfig config = AppConfig.getInstance();
        String host = config.getProperty("host");
        String port = config.getProperty("port");
        String username = config.getProperty("ftp_user");
        String password = config.getProperty("ftp_password");
        String srcDir = fixRegularPath(config.getProperty("src_dir"));
        String destDir = fixRegularPath(config.getProperty("dest_dir"));
        String _overwrite = config.getProperty("overwrite");
        boolean overwrite = "true".equals(_overwrite);

        FTPConfig ftpconfig = new FTPConfig();
        ftpconfig.setUser(username);
        ftpconfig.setPassword(password);
        ftpconfig.setHost(host);
        ftpconfig.setPort(Integer.valueOf(port));

        ftpconfig.setType(FTPConfig.TYPE_FTPS);
        ftpconfig.setBuffSize(10240);
        ftpconfig.setDataWaitTimeout(10000);
        ftpconfig.setMode(FTPConfig.PASSIVE_MODE);
        ftpconfig.setConnectTimeout(30000);

        util = new SFTPUtils(ftpconfig);
        boolean success = util.connect();
        while(!success) {
            try {
                Thread.sleep(5000);
                success = util.connect();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(!util.cd(srcDir)) {
            throw new IllegalStateException("无法改变到工作目录：" + srcDir);
        }

        File destFolder = new File(destDir);
        if(!destFolder.exists()) {
            destFolder.mkdirs();
        }

        for(;;) {
            try {
                List<String> list = listFiles(srcDir, new ArrayList<String>(), "", true);
                for (String s : list) {
                    util.download(s, destDir + "/" + s, !overwrite, new DownloadMonitor(s));
                }
            } catch (Exception e) {
                e.printStackTrace();
                util.disconnect();
                success = util.connect();
                while(!success) {
                    try {
                        Thread.sleep(5000);
                        success = util.connect();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
                if(!util.cd(srcDir)) {
                    throw new IllegalStateException("无法改变到工作目录：" + srcDir);
                }
            } finally {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                }
            }
        }
    }


    private List<String> listFiles(String path, List<String> list, String prefix, boolean root) {
        Vector<ChannelSftp.LsEntry> children = util.ls(path);
        if(null != children) {
            for (ChannelSftp.LsEntry entry : children) {
                if(entry.getAttrs().isDir()) {
                    listFiles(path + "/" + entry.getFilename(), list,
                            root ? entry.getFilename() : (prefix + "/" + entry.getFilename()), false);
                } else {
                    String ret = FilterChain.filter(entry.getFilename());
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
