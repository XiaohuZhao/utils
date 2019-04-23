package com.yorma.common.utils.collection;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 集合工具类
 *
 * @author zxh
 */
public class CollectionUtil {

    /**
     * 将实体按照每size个分一组
     *
     * @param list 要分隔的实体集合
     */
    public static <T> List<List<T>> splitEntities(List<T> list, int size) {
        // 计算组数
        int limit = (list.size() + size - 1) / size;
        return Stream.iterate(0, n -> n + 1)
                .limit(limit).parallel()
                .map(a -> list.stream().skip(a * size).limit(size).parallel().collect(Collectors.toList()))
                .collect(Collectors.toList());
    }
}
