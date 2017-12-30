package com.hty.util.filesync.filter;

public interface Filter {
    /**
     * 根据配置文件的配置过滤条件过滤不符合exclude条件的文件，并返回，
     * 如果此文件符合过滤条件，则返回null
     */
    String filter(String file);

    /**
     * 添加过滤器
     */
    void addFilter(Filter filter);

    String getName() ;

    String getPattern();

}
