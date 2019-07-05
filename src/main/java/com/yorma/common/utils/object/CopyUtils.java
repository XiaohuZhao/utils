package com.yorma.common.utils.object;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author 窦琪
 * @date 2018年1月10日
 */
public class CopyUtils {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CopyUtils.class);
	   
    /**  
     * 无需进行复制的特殊类型数组  
     */
    private final static Class[] NEEDLESS_CLONE_CLASSES = new Class[]{String.class, Boolean.class, Character.class, Byte.class, Short.class,
        Integer.class,Long.class,Float.class,Double.class,Void.class,Object.class,Class.class    
    };    
    /**  
     * 判断该类型对象是否无需复制  
     * @param c 指定类型  
     * @return 如果不需要复制则返回真，否则返回假  
     */    
    private static boolean isNeedlessClone(final Class c){
	    //基本类型
	    if (c.isPrimitive()) {
            return true;
	    }
	    //是否在无需复制类型数组里
	    return Arrays.asList(NEEDLESS_CLONE_CLASSES).contains(c);
    }    
        
    /**  
     * 尝试创建新对象  
     * @param value 原始对象
     * @return 新的对象
     * @throws IllegalAccessException 访问权限异常
     */    
    private static Object createObject(Object value) throws IllegalAccessException{    
            try {    
                return value.getClass().newInstance();    
            } catch (InstantiationException e) {
	            return null;
            }
    }    
        
    /**  
     * 复制对象数据  
     * @param value 原始对象  
     * @param level 复制深度。小于0为无限深度，即将深入到最基本类型和Object类级别的数据复制；  
     * 大于0则按照其值复制到指定深度的数据，等于0则直接返回对象本身而不进行任何复制行为。  
     * @return 返回复制后的对象
     */
    public static Object clone(Object value, int level) {
        if(value==null){    
            return null;
        }
	    if (level == 0) {
		    return value;
	    }
	    Class c = value.getClass();
        if(isNeedlessClone(c)){    
            return value;    
        }
	    final AtomicInteger atomicLevel = new AtomicInteger(level);
	    //复制新的集合
	    if (value instanceof Collection) {
		    value = ((Collection) value).stream().map(o -> clone(o, atomicLevel.decrementAndGet())).collect(Collectors.toList());
	    }
	    //复制新的Array
	    else if (c.isArray()) {
		    value = copyArray(value, level);
	    }
	    //复制新的MAP
	    else if (value instanceof Map) {
		    final Set<Entry> set = ((Map) value).entrySet();
		    value = set.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	    }
        else {
		    try {
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
		    } catch (Exception e) {
			    throw new RuntimeException(e);
		    }
	    }
	    return value;
    }
	
	private static Object copyArray(Object value, final int level) {
		final Class<?> c = value.getClass();
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
			IntStream.range(0, old.length).forEach(i -> tmp[i] = clone(old[i], level));
			value = tmp;
		}
		return value;
	}
	
	/**
     * 浅表复制对象  
     * @param value 原始对象  
	 * @return 复制后的对象，只复制一层
	 */
	public static Object clone(Object value) {
        return clone(value,1);    
    }    
        
    /**  
     * 深度复制对象  
     * @param value 原始对象  
     * @return 复制后的对象
     */
    public static Object deepClone(Object value) {
        return clone(value,-1);    
    } 

    /**
     * 将一个对象转换为另一个对象
     * @param <T1> 要转换的对象
     * @param <T2> 转换后的类
     * @param orimodel 要转换的对象
     * @param newObj 转换后的类
     * @return 转换后的对象
     */
    public static  <T1,T2> T2 convertBean(T1 orimodel, T2 newObj) {
	    //要转换的字段集合
	    List<Field> fieldlist = new ArrayList<>();
	    //循环获取要转换的字段,包括父类的字段
	    for (Class castClass = newObj.getClass(); castClass != null && !Object.class.equals(castClass); castClass = (Class<T2>) castClass.getSuperclass()) {
		    fieldlist.addAll(Arrays.asList(castClass.getDeclaredFields()));
	    }
	 
	    // ZJ @ 2019-05-10 修改后的方法支持非标准的get,set属性
	    fieldlist.stream()
			    .filter(field -> !(setVal1(field, orimodel, newObj) || setVal2(field, orimodel, newObj) || setVal3(field, orimodel, newObj)))
			    .forEach(field -> LOGGER.error("{} ==> {} 复制值 {} 失败!\r\n", orimodel.getClass().getName(), newObj.getClass().getName(), field.getName()));
	    return newObj;
    }

    /**
     *
     * @param field 要设置值的属性
     * @param orimodel 原始对象
     * @param returnModel 转换后的对象
     * @return 设置是否成功
     */
    private static <T1, T2> boolean setVal1(Field field, T1 orimodel, T2 returnModel) {
	    boolean b = false;

		try {
	        PropertyDescriptor getpd = new PropertyDescriptor(field.getName(), orimodel.getClass());
	        PropertyDescriptor setpd=new PropertyDescriptor(field.getName(), returnModel.getClass());
	
	        Method getMethod = getpd.getReadMethod();
	        Object transValue = getMethod.invoke(orimodel);
	        Method setMethod = setpd.getWriteMethod();
	        setMethod.invoke(returnModel, transValue);
	        b = true;
		} catch (Exception e) {
//			throw new RuntimeException(e);不可抛出
		}
        return b;
	}

    /**
     *
     * @param field 要设置值的属性
     * @param orimodel 原始对象
     * @param returnModel 转换后的对象
     * @return 设置是否成功
     */
    private static <T1, T2> boolean setVal2(Field field, T1 orimodel, T2 returnModel) {
    	boolean b = false;

    	try {
	    	String xetter = field.getName().substring(0,1).toUpperCase() + field.getName().substring(1);
			Method getMethod = orimodel.getClass().getMethod("get" + xetter);
			Object transValue = getMethod.invoke(orimodel);
	
			String setter = "set" + xetter;
			Method mySetter = null;
			try {
				mySetter = returnModel.getClass().getMethod(setter, field.getType());
				mySetter.invoke(returnModel, transValue);
				b = true;
			} catch (NoSuchMethodException e) {
				// setter: Long-->long Integer-->int
				if( field.getType().equals(Long.class)){
					mySetter = returnModel.getClass().getMethod(setter, long.class);
				}
				if( field.getType().equals(Integer.class)){
					mySetter = returnModel.getClass().getMethod(setter, int.class);
				}
				if (mySetter != null) {
					mySetter.invoke(returnModel, transValue);
					b = true;
				}
			}
	    } catch (Exception e) {
//		    throw new RuntimeException(e);不可抛出
		}
    	return b;
    }
	
	/**
	 * @param field
	 * 		要设置值的属性
	 * @param orimodel
	 * 		原始对象
	 * @param returnModel
	 * 		转换后的对象
	 * @return 设置是否成功
	 */
    private static <T1, T2> boolean setVal3(Field field, T1 orimodel, T2 returnModel) {
    	boolean b = false;
    	try {
	    	String xetter = field.getName();
			Method getMethod = orimodel.getClass().getMethod("get" + xetter);
			Object transValue = getMethod.invoke(orimodel);
	
			String setter = "set" + xetter;
			Method mySetter = null;
			try {
				mySetter = returnModel.getClass().getMethod(setter, field.getType());
				mySetter.invoke(returnModel, transValue);
				b = true;
			}catch(NoSuchMethodException e){// setter: Long-->long Integer-->int
				if( field.getType().equals(Long.class)){
					mySetter = returnModel.getClass().getMethod(setter, long.class);
				}
				if( field.getType().equals(Integer.class)){
					mySetter = returnModel.getClass().getMethod(setter, int.class);
				}
				Objects.requireNonNull(mySetter).invoke(returnModel, transValue);
				b = true;
			}
	    } catch (Exception e) {
//		    throw new RuntimeException(e);不可抛出
	    }
    	return b;
    }
}
