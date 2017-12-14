package com.hty.util.filesync.client;

import com.hty.baseframe.jproxy.bean.RemoteService;
import com.hty.baseframe.jproxy.common.ServiceFactory;
import com.hty.util.filesync.handler.FTPDownloader;
import com.hty.util.filesync.service.FileService;
import com.hty.util.filesync.util.AppConfig;
import com.hty.util.filesync.util.DirectotyScanUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FileSyncClient implements Runnable {

    public FileSyncClient() {
        AppConfig config = AppConfig.getInstance();
        String host = config.getProperty("host");
        String port = config.getProperty("listen");
        RemoteService remoteService = new RemoteService(FileService.class, host, port, null);
        ServiceFactory.addRemoteService(remoteService);
    }

    @Override
    public void run() {

        AppConfig config = AppConfig.getInstance();
        String destDir = config.getProperty("dest_dir");

        for(;;) {
            try {
                Set<String> set = new HashSet<String>();
//                DirectotyScanUtil.scan(new File(destDir), "", set, true, true);
                Set<Integer> hashes = new HashSet<Integer>();
                for (String s : set) {
                    hashes.add(s.hashCode());
                }
                FileService fs = ServiceFactory.getProxyInstance(FileService.class);
                Set<String> ret =  fs.compare(hashes);
                System.out.println(ret);
                FTPDownloader downloader = FTPDownloader.getInstance();
                try {
                    downloader.download(ret);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
