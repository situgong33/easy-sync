package com.hty.util.filesync;

import com.hty.util.filesync.client.FTPSyncClient;
import com.hty.util.filesync.client.SFTPSyncClient;
import com.hty.util.filesync.util.AppConfig;

/**
 * 程序启动类
 * @version 1.0
 * @author Hetianyi 12/07/2017
 */
public class Main {


    public static void main(String[] args) {
        AppConfig config = AppConfig.getInstance();
        String ftp_type = config.getProperty("ftp_type");
        if(null == ftp_type ||
                (!"ftp".equals(ftp_type) && !"sftp".equals(ftp_type))) {
            throw new IllegalArgumentException("Error: 'ftp_type' must be 'ftp' or 'sftp'.");
        }

        Runnable service;
        if("sftp".equals(ftp_type)) {
            service = new SFTPSyncClient();
        } else {
            service = new FTPSyncClient();
        }
        Thread t = new Thread(service);
        t.start();
    }

}
