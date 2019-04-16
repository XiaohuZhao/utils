package com.yorma.common.utils.generator;

/**
 * 传输类转换成xml实体类继承的接口
 *
 * @author zxh
 * @version 1.0.0
 * @date 2019/03/28
 * @since 1.0.0
 */
public interface Transformable<T> {
    /**
     * 将对象转换成可生成报文的对象
     *
     * @param t 要转换的对象
     * @return 可转换的对象
     */
    Transformable transform(T t);
}
