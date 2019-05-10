package com.yorma.common.utils.object;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

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
            throw new NullPointerException("必要的对象或属性值为空！");
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
     * <p>判断对象是否为空</p>
     * <p>引用指向的地址为空</p>
     * <p>指向的值不存在</p>
     * <p>字符串的长度为0</p>
     * <p>集合的长度为0</p>
     * <p>数组(引用类型)长度为0</p>
     * <p>实体对象的每个属性都为空</p>
     *
     * @param t   要判断的对象
     * @param <T> 参数的类型
     * @return 是否为空
     */
    public static <T> boolean isEmpty(T t) {
        if (null == t) {
            return true;
        } else if (t instanceof Optional) {
            return !((Optional) t).isPresent();
        } else if (t instanceof String) {
            return ((String) t).trim().length() == 0;
        } else if (t instanceof Collection) {
            return CollectionUtils.isEmpty((Collection) t);
        } else if (t instanceof Map) {
            return CollectionUtils.isEmpty((Map) t);
        } else if (t.getClass().isArray()) {
            return ArrayUtils.isEmpty((Object[]) t);
        } else {
            return Arrays.stream(t.getClass().getDeclaredFields()).noneMatch(field -> {
                field.setAccessible(true);
                try {
                    return field.get(t) != null;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * <p>判断对象是否不为空</p>
     * <p>引用指向的地址不为空</p>
     * <p>指向的值存在</p>
     * <p>字符串的长度大于0</p>
     * <p>集合的长度大于0</p>
     * <p>数组(引用类型)长度大于0</p>
     * <p>实体对象至少有一个属性不为空</p>
     *
     * @param t   要判断的对象
     * @param <T> 参数的类型
     * @return 是否为空
     */
    public static <T> boolean isNotEmpty(T t) {
        return !isEmpty(t);
    }

    /**
     * 将对象转成int类型的数字
     *
     * @param o 要转的对象
     * @return 对象中的数字
     */
    public static int parseInt(Object o) {
        if (isEmpty(o)) {
            throw new NullPointerException("转换的对象不可为空");
        }
        return Integer.parseInt(o.toString());
    }

    public static double parseDouble(Object o) {
        if (isEmpty(o)) {
            throw new NullPointerException("转换的对象不可为空");
        }
        return Double.parseDouble(o.toString());
    }
}
