package com.hty.util.filesync.util;

import com.hty.baseframe.jproxy.common.BeanProvider;
import com.hty.util.filesync.service.FileService;
import com.hty.util.filesync.service.impl.FileServiceImpl;

public class SimpleServiceProvider implements BeanProvider {

    private FileService service;

    @Override
    public Object getBean(Class<?> aClass) {
        if(null == service) {
            synchronized (this) {
                if(null == service) {
                    service = new FileServiceImpl();
                }
            }
        }
        return service;
    }
}
