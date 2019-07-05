package com.yorma.common.utils.collection;

/**
 * @author zxh
 * @version 1.0.0
 * @date 2019-05-06 17:07
 * @since 1.0.0
 */
@FunctionalInterface
public interface TriConsumer<V1, V2,V3> {
    /**
     * 接受三个参数没有返回值的函数式接口
     * @param v1 值1
     * @param v2 值2
     * @param v3 值3
     */
    void accept(V1 v1,V2 v2,V3 v3);
}
