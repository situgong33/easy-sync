package com.hty.util.filesync.filter.impl;

import com.hty.util.filesync.filter.Filter;

import java.io.File;

public class ExcludeFilterImpl implements Filter {


    private String name;
    private String pattern;

    private Filter followedFilter;

    public ExcludeFilterImpl(String name, String pattern) {
        //Run simple test
        "".matches(pattern);
        this.name = name;
        this.pattern = pattern;
    }

    @Override
    public File filter(File file) {
        if(null != file && !file.getName().matches(pattern)) {
            if(null != followedFilter) {
                return followedFilter.filter(file);
            } else {
                return file;
            }
        } else {
            return null;
        }
    }

    @Override
    public void addFilter(Filter filter) {
        if(null != filter) {
            if(null != followedFilter) {
                followedFilter.addFilter(filter);
            } else {
                followedFilter = filter;
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getPattern() {
        return pattern;
    }
}
