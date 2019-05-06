package com.yorma.common.utils.object;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author 窦琪
 * @date 2018年1月10日
 */
public class CopyUtils {

    /**
     * 无需进行复制的特殊类型数组
     */
    private static final Class[] NEEDLESS_CLONE_CLASSES = new Class[]{String.class, Boolean.class, Character.class, Byte.class, Short.class,
            Integer.class, Long.class, Float.class, Double.class, Void.class, Object.class, Class.class
    };

    /**
     * 判断该类型对象是否无需复制
     *
     * @param c 指定类型
     * @return 如果不需要复制则返回真，否则返回假
     */
    private static boolean isNeedlessClone(Class c) {
        //基本类型
        if (c.isPrimitive()) {
            return true;
        }
        //是否在无需复制类型数组里
        return Arrays.stream(NEEDLESS_CLONE_CLASSES).findFirst().filter(c::equals).isPresent();
    }

    /**
     * 尝试创建新对象
     *
     * @param value 原始对象
     * @return 新的对象
     * @throws IllegalAccessException 没有权限异常
     */
    private static Object createObject(Object value) throws IllegalAccessException {
        try {
            return value.getClass().newInstance();
        } catch (InstantiationException e) {
            return null;
        }
    }

    /**
     * 复制对象数据
     *
     * @param value 原始对象
     * @param level 复制深度。小于0为无限深度，即将深入到最基本类型和Object类级别的数据复制；
     *              大于0则按照其值复制到指定深度的数据，等于0则直接返回对象本身而不进行任何复制行为。
     * @return 返回复制后的对象
     * @throws IllegalAccessException 没有权限异常
     * @throws InstantiationException 初始化异常
     */
    public static Object clone(Object value, int level) throws IllegalAccessException, InstantiationException {
        if (value == null) {
            return null;
        }
        if (level == 0) {
            return value;
        }
        Class c = value.getClass();
        if (isNeedlessClone(c)) {
            return value;
        }
        level--;
        //复制新的集合
        if (value instanceof Collection) {
            Collection tmp = (Collection) c.newInstance();
            for (Object v : (Collection) value) {
                //深度复制
                tmp.add(clone(v, level));
            }
            value = tmp;
            //复制新的Array
        } else if (c.isArray()) {
            value = cloneArray(value, level, c);
            //复制新的MAP
        } else if (value instanceof Map) {
            Map tmp = (Map) c.newInstance();
            Map org = (Map) value;
            for (Object key : org.keySet()) {
                //深度复制
                tmp.put(key, clone(org.get(key), level));
            }
            value = tmp;
        } else {
            Object tmp = createObject(value);
            //无法创建新实例则返回对象本身，没有克隆
            if (tmp == null) {
                return value;
            }
            Set<Field> fields = new HashSet<>();
            while (c != null && !c.equals(Object.class)) {
                fields.addAll(Arrays.asList(c.getDeclaredFields()));
                c = c.getSuperclass();
            }
            for (Field field : fields) {
                //仅复制非final字段
                if (!Modifier.isFinal(field.getModifiers())) {
                    field.setAccessible(true);
                    //深度复制
                    field.set(tmp, clone(field.get(value), level));
                }
            }
            value = tmp;
        }
        return value;
    }

    private static Object cloneArray(Object value, final int level, final Class c) throws IllegalAccessException, InstantiationException {
        //首先判断是否为基本数据类型
        if (c.equals(int[].class)) {
            int[] old = (int[]) value;
            value = Arrays.copyOf(old, old.length);
        } else if (c.equals(short[].class)) {
            short[] old = (short[]) value;
            value = Arrays.copyOf(old, old.length);
        } else if (c.equals(char[].class)) {
            char[] old = (char[]) value;
            value = Arrays.copyOf(old, old.length);
        } else if (c.equals(float[].class)) {
            float[] old = (float[]) value;
            value = Arrays.copyOf(old, old.length);
        } else if (c.equals(double[].class)) {
            double[] old = (double[]) value;
            value = Arrays.copyOf(old, old.length);
        } else if (c.equals(long[].class)) {
            long[] old = (long[]) value;
            value = Arrays.copyOf(old, old.length);
        } else if (c.equals(boolean[].class)) {
            boolean[] old = (boolean[]) value;
            value = Arrays.copyOf(old, old.length);
        } else if (c.equals(byte[].class)) {
            byte[] old = (byte[]) value;
            value = Arrays.copyOf(old, old.length);
        } else {
            Object[] old = (Object[]) value;
            Object[] tmp = Arrays.copyOf(old, old.length, old.getClass());
            for (int i = 0; i < old.length; i++) {
                tmp[i] = clone(old[i], level);
            }
            value = tmp;
        }
        return value;
    }

    /**
     * 浅表复制对象
     *
     * @param value 原始对象
     * @return 复制后的对象，只复制一层
     * @throws IllegalAccessException 没有权限异常
     * @throws InstantiationException 初始化异常
     */
    public static Object clone(Object value) throws IllegalAccessException, InstantiationException {
        return clone(value, 1);
    }

    /**
     * 深度复制对象
     *
     * @param value 原始对象
     * @return 复制后的对象
     * @throws IllegalAccessException 没有权限异常
     * @throws InstantiationException 初始化异常
     */
    public static Object deepClone(Object value) throws IllegalAccessException, InstantiationException {
        return clone(value, -1);
    }

    /**
     * 将一个对象转换为另一个对象
     *
     * @param <T1>      要转换的对象
     * @param <T2>      转换后的类
     * @param orimodel  要转换的对象
     * @param castClass 转换后的类
     * @return 转换后的对象
     */
    public static <T1, T2> T2 convertBean(T1 orimodel, Class<T2> castClass) {
        T2 returnModel;
        try {
            returnModel = castClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("创建" + castClass.getName() + "对象失败");
        }
        //要转换的字段集合
        List<Field> fieldlist = new ArrayList<>();
        //循环获取要转换的字段,包括父类的字段
        while (castClass != null &&
                !"java.lang.object".equals(castClass.getName().toLowerCase())) {
            fieldlist.addAll(Arrays.asList(castClass.getDeclaredFields()));
            //得到父类,然后赋给自己
            castClass = (Class<T2>) castClass.getSuperclass();
        }
        for (Field field : fieldlist) {
            PropertyDescriptor getpd;
            PropertyDescriptor setpd;
            try {
                getpd = new PropertyDescriptor(field.getName(), orimodel.getClass());
                setpd = new PropertyDescriptor(field.getName(), returnModel.getClass());
            } catch (Exception e) {
                continue;
            }
            try {
                Method getMethod = getpd.getReadMethod();
                Object transValue = getMethod.invoke(orimodel);
                Method setMethod = setpd.getWriteMethod();
                setMethod.invoke(returnModel, transValue);
            } catch (Exception e) {
                assert castClass != null;
                throw new RuntimeException("cast " + orimodel.getClass().getName() + "to "
                        + castClass.getName() + " failed");
            }
        }
        return returnModel;
    }
}
