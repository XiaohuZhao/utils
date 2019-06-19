package com.yorma.common.utils.generator;

import javax.xml.bind.JAXBException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static com.yorma.common.utils.generator.XmlGenerator.generateXmlStr;

/**
 * 传输类转换成xml实体类继承的接口
 *
 * @author zxh
 * @version 1.0.0
 * @date 2019/03/28
 * @since 1.0.0
 */
public interface Transformable<T> extends Serializable {
    /**
     * 将对象转换成可生成报文的对象
     *
     * @param t 要转换的对象
     * @return 可转换的对象
     */
    Transformable transform(T t);
    
    /**
     * <p>将实现Transformable接口的实体类对象</p>
     * <p>按照自定义的转换规则重写的transform(T)方法</p>
     * <p>转换成xml格式的字符串</p>
     *
     * @param aClass
     * 		报文对应的Xml实体类型
     * @param xmlObj
     * 		要转成xml字符串的实体类
     * @param params
     * 		构造器的参数
     * @return 报文xml字符串
     * @throws NoSuchMethodException
     * 		实体类没有重写transform(T) 方法
     * @throws IllegalAccessException
     * 		方法访问权限异常, 不会出现
     * @throws InvocationTargetException
     * 		调用目标时出现异常, 实现的transform(T) 方法中抛出异常时出现
     * @throws InstantiationException
     * 		实例化出现异常, 一般是构造器参数不正确
     * @throws JAXBException
     * 		xml 对应的实体解析时出现异常
     */
    static String toXmlStr(Class aClass, Object xmlObj, Object... params) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, JAXBException {
        Class[] classes = Arrays.stream(params).map(Object::getClass).toArray(Class[]::new);
        final Constructor constructor = aClass.getDeclaredConstructor(classes);
        final Object instance = constructor.newInstance(params);
        final Method transform = aClass.getDeclaredMethod("transform", xmlObj.getClass());
        final Transformable transformable = (Transformable) transform.invoke(instance, xmlObj);
        return generateXmlStr(transformable);
    }
}