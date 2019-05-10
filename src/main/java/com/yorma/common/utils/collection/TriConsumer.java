package com.yorma.common.utils.collection;

/**
 * @author zxh
 * @version 1.0.0
 * @date 2019-05-06 17:07
 * @since 1.0.0
 */
@FunctionalInterface
public interface TriConsumer<K1,K2,V> {
    void accept(K1 k1,K2 k2,V v);
}
