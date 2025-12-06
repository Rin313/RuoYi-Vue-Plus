package org.dromara.common.mybatis.core;

import java.util.Collections;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.hutool.core.collection.CollUtil;

public class PageUtils {
    
    public static <T> IPage<T> toPage(List<T> list, Long pageNum, Long pageSize) {
        if (CollUtil.isEmpty(list)) {
            return new Page<>(pageNum, pageSize, 0);
        }
        
        int total = list.size();
        int start = (int) ((pageNum - 1) * pageSize);
        int end = (int) Math.min(start + pageSize, total);
        
        Page<T> page = new Page<>(pageNum, pageSize, total);
        page.setRecords(start >= total ? Collections.emptyList() : list.subList(start, end));
        return page;
    }
    public static <T> IPage<T> toPage(List<T> list) {
        Page<T> page = new Page<>(1, Integer.MAX_VALUE, list.size());
        page.setRecords(list);
        return page;
    }
}