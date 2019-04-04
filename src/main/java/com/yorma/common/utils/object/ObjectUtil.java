package com.yorma.common.utils.object;

/**
 * 处理对象的一些方法
 *
 * @author zxh
 * @version 1.0.0
 * @date 2019/03/28
 * @since 1.0.0
 */
public class ObjectUtil {

    /**
     * 处理必要的对象，如果不为空，返回原对象，否则报错
     */
    public static <T> T required(T t) {
        if (isEmpty(t)) {
            throw new NullPointerException("必要的属性值为空！");
        }
        return t;
    }

    /**
     * 处理可选的对象，如果不为空，返回原对象，否则返回null
     */
    public static <T> T optional(T t) {
        if (isEmpty(t)) {
            return null;
        }
        return t;
    }

    /**
     * 处理有默认值的对象，如果不为空，返回原对象，否则返回默认值
     */
    public static <T> T selective(T t, T defaultValue) {
        if (isEmpty(t)) {
            return defaultValue;
        }
        return t;
    }

    /**
     * 判断对象是否为空
     *
     * @param t   要判断的对象
     * @param <T> 参数的类型
     * @return 是否为空
     */
    public static <T> boolean isEmpty(T t) {
        return null == t || t.toString().trim().isEmpty();
    }

}
