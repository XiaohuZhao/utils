package com.yorma.common.utils.object;

import java.lang.reflect.Field;    
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;    
import java.util.Collection;    
import java.util.HashSet;
import java.util.List;
import java.util.Map;    
import java.util.Set;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;  
/**
 * @author 窦琪
 * @2018年1月10日
 */
public class CopyUtils {
	   
    /**  
     * 无需进行复制的特殊类型数组  
     */    
    static Class[] needlessCloneClasses = new Class[]{String.class,Boolean.class,Character.class,Byte.class,Short.class,    
        Integer.class,Long.class,Float.class,Double.class,Void.class,Object.class,Class.class    
    };    
    /**  
     * 判断该类型对象是否无需复制  
     * @param c 指定类型  
     * @return 如果不需要复制则返回真，否则返回假  
     */    
    private static boolean isNeedlessClone(Class c){
        if(c.isPrimitive()){//基本类型    
            return true;    
        }    
        for(Class tmp:needlessCloneClasses){//是否在无需复制类型数组里    
            if(c.equals(tmp)){    
                return true;    
            }    
        }    
        return false;    
    }    
        
    /**  
     * 尝试创建新对象  
     * @param c 原始对象  
     * @return 新的对象  
     * @throws IllegalAccessException  
     */    
    private static Object createObject(Object value) throws IllegalAccessException{    
            try {    
                return value.getClass().newInstance();    
            } catch (InstantiationException e) {    
                return null;    
            } catch (IllegalAccessException e) {    
                throw e;    
            }    
    }    
        
    /**  
     * 复制对象数据  
     * @param value 原始对象  
     * @param level 复制深度。小于0为无限深度，即将深入到最基本类型和Object类级别的数据复制；  
     * 大于0则按照其值复制到指定深度的数据，等于0则直接返回对象本身而不进行任何复制行为。  
     * @return 返回复制后的对象  
     * @throws IllegalAccessException  
     * @throws InstantiationException  
     * @throws InvocationTargetException  
     * @throws NoSuchMethodException  
     */    
    public static Object clone(Object value,int level) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException{
    	if(value==null){
            return null;    
        }    
        if(level==0){    
            return value;    
        }    
        Class c = value.getClass();    
        if(isNeedlessClone(c)){    
            return value;    
        }    
        level--;    
        if(value instanceof Collection){//复制新的集合    
            Collection tmp = (Collection)c.newInstance();    
            for(Object v:(Collection)value){    
                tmp.add(clone(v,level));//深度复制    
            }    
            value = tmp;    
        }    
        else if(c.isArray()){//复制新的Array    
            //首先判断是否为基本数据类型    
            if(c.equals(int[].class)){    
                int[] old = (int[])value;    
                value = (int[])Arrays.copyOf(old, old.length);    
            }    
            else if(c.equals(short[].class)){    
                short[] old = (short[])value;    
                value = (short[])Arrays.copyOf(old, old.length);    
            }    
            else if(c.equals(char[].class)){    
                char[] old = (char[])value;    
                value = (char[])Arrays.copyOf(old, old.length);    
            }    
            else if(c.equals(float[].class)){    
                float[] old = (float[])value;    
                value = (float[])Arrays.copyOf(old, old.length);    
            }    
            else if(c.equals(double[].class)){    
                double[] old = (double[])value;    
                value = (double[])Arrays.copyOf(old, old.length);    
            }    
            else if(c.equals(long[].class)){    
                long[] old = (long[])value;    
                value = (long[])Arrays.copyOf(old, old.length);    
            }    
            else if(c.equals(boolean[].class)){    
                boolean[] old = (boolean[])value;    
                value = (boolean[])Arrays.copyOf(old, old.length);    
            }    
            else if(c.equals(byte[].class)){    
                byte[] old = (byte[])value;    
                value = (byte[])Arrays.copyOf(old, old.length);    
            }    
            else {    
                Object[] old = (Object[])value;    
                Object[] tmp = (Object[])Arrays.copyOf(old, old.length, old.getClass());    
                for(int i = 0;i<old.length;i++){    
                    tmp[i] = clone(old[i],level);    
                }    
                value = tmp;    
            }    
        }    
        else if(value instanceof Map){//复制新的MAP    
            Map tmp = (Map)c.newInstance();    
            Map org = (Map)value;    
            for(Object key:org.keySet()){    
                tmp.put(key, clone(org.get(key),level));//深度复制    
            }    
            value = tmp;    
        }    
        else {    
            Object tmp = createObject(value);    
            if(tmp==null){//无法创建新实例则返回对象本身，没有克隆    
                return value;    
            }    
            Set<Field> fields = new HashSet<Field>();    
            while(c!=null&&!c.equals(Object.class)){    
                fields.addAll(Arrays.asList(c.getDeclaredFields()));    
                c = c.getSuperclass();    
            }    
            for(Field field:fields){    
                if(!Modifier.isFinal(field.getModifiers())){//仅复制非final字段    
                    field.setAccessible(true);    
                    field.set(tmp, clone(field.get(value),level));//深度复制    
                }    
            }    
            value = tmp;    
        }    
        return value;    
    }    
        
    /**  
     * 浅表复制对象  
     * @param value 原始对象  
     * @return 复制后的对象，只复制一层  
     * @throws IllegalAccessException  
     * @throws InstantiationException  
     * @throws InvocationTargetException  
     * @throws NoSuchMethodException  
     */    
    public static Object clone(Object value) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException{    
        return clone(value,1);    
    }
    
    private static Object cloneArray(Object value, final int level, final Class c) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
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
     * 深度复制对象  
     * @param value 原始对象  
     * @return 复制后的对象  
     * @throws IllegalAccessException  
     * @throws InstantiationException  
     * @throws InvocationTargetException  
     * @throws NoSuchMethodException  
     */    
    public static Object deepClone(Object value) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException{    
        return clone(value,-1);    
    } 

    /**
     * 将一个对象转换为另一个对象
     * @param <T1> 要转换的对象
     * @param <T2> 转换后的类
     * @param orimodel 要转换的对象
     * @param castClass 转换后的类
     * @return 转换后的对象
     */
    public static  <T1,T2> T2 convertBean(T1 orimodel, T2 newObj) {
        T2 returnModel = newObj;
        Class castClass=null;
        try {
           castClass = newObj.getClass();
        } catch (Exception e) {
            throw new RuntimeException("创建"+castClass.getName()+"对象失败");
        } 
        List<Field> fieldlist = new ArrayList<Field>(); //要转换的字段集合
        while (castClass != null && //循环获取要转换的字段,包括父类的字段
                !castClass.getName().toLowerCase().equals("java.lang.object")) {
            fieldlist.addAll(Arrays.asList(castClass.getDeclaredFields()));
            castClass = (Class<T2>) castClass.getSuperclass(); //得到父类,然后赋给自己
        }

		for (Field field : fieldlist) {
			// ZJ @ 2019-05-10 修改后的方法支持非标准的get,set属性
			if(setVal1(field, orimodel, returnModel) || setVal2(field, orimodel, returnModel)
					|| setVal3(field, orimodel, returnModel)) {
			}else {
				System.out.printf("%s ==> %s 复制值 %s 失败!\r\n", orimodel.getClass().getName(), returnModel.getClass().getName(), field.getName());
//				new RuntimeException("cast "+ orimodel.getClass().getName()+" to "
//	                        + returnModel.getClass().getName()+" failed").printStackTrace();
            }
        }

        return returnModel;
    }

    /**
     * 
     * @param field
     * @param orimodel
     * @param returnModel
     * @return
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
//			e.printStackTrace();
		}

        return b;
	}

    /**
     * 
     * @param field
     * @param orimodel
     * @param returnModel
     * @return
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
			}catch(NoSuchMethodException e){// setter: Long-->long Integer-->int
				if( field.getType().equals(Long.class)){
					mySetter = returnModel.getClass().getMethod(setter, long.class);
				}
				if( field.getType().equals(Integer.class)){
					mySetter = returnModel.getClass().getMethod(setter, int.class);
				}
				if(transValue != null)
					mySetter.invoke(returnModel, transValue);
				b = true;
			}
    	} catch (Exception e1) {
//    		e1.printStackTrace();
		}

    	return b;
    }
    
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
				if(transValue != null)
					mySetter.invoke(returnModel, transValue);
				b = true;
			}
    	} catch (Exception e1) {
//    		e1.printStackTrace();
		}

    	return b;
    }
}
