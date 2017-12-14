package com.hty.util.filesync.service.impl;

import com.hty.baseframe.jproxy.common.SysProprties;
import com.hty.util.filesync.service.FileService;
import com.hty.util.filesync.util.AppConfig;
import com.hty.util.filesync.util.DirectotyScanUtil;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FileServiceImpl implements FileService {

    @Override
    public Set<String> compare(Set<Integer> fileNameHash) {
        Set<String> ret = new HashSet<String>();
        AppConfig config = AppConfig.getInstance();
        String srcDir = config.getProperty("src_dir");
        Set<String> set = new HashSet<String>();
        DirectotyScanUtil.scan(new File(srcDir), "", set, true, false);
        System.out.println(fileNameHash);
        System.out.println(set);
        if(null == fileNameHash) {
            fileNameHash = new HashSet<Integer>();
        }
        for(Iterator<String> it = set.iterator(); it.hasNext();) {
            String path = it.next();
            if(!fileNameHash.contains(path.hashCode())) {
                ret.add(path);
            } else {
                System.out.println("客户端已存在：" + path);
            }
        }
        return ret;
    }
}
