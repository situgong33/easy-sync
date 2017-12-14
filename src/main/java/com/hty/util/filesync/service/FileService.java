package com.hty.util.filesync.service;

import java.util.Set;

public interface FileService {

    /**
     * 客户端将文件的相对路径转成hash对比
     * @param fileNameHash
     * @return
     */
    Set<String> compare(Set<Integer> fileNameHash);

}
