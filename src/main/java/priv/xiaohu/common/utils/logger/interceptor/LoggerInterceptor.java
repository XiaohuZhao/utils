package priv.xiaohu.common.utils.logger.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import priv.xiaohu.common.utils.logger.annotation.SysLogger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 拦截方法,判断是否有@SysLogger注解并采取动作
 *
 * @author zxh
 * @version 1.0.0
 * @since 1.0.1
 */
@Aspect
@Component
public class LoggerInterceptor {
	private static final String LOG_FORMAT = "->{}: {}";
	private static final String BEGIN = "STR";
	private static final String END = "END";
	private static final String PARAMS = "PAM";
	private static final String RESULT = "RST";
	private static final String TIME = "TIM";
	private static final String CURRENT_THREAD = "TRD";
	private static Logger LOGGER;
	private static final String RESPONSE_MESSAGE = "com.yorma.common.entity.dto.ResponseMessage";
	private static final String RESPONSE_DATA = "com.yorma.common.entity.dto.ResponseData";
	private static final int LIMIT_SIZE = 1;
	
	private String toJsonString(Object object) {
		ObjectMapper objectMapper = new ObjectMapper();
		String result = null;
		if (object != null) {
			try {
				StringBuffer resultBuilder = new StringBuffer();
				Object l = simplifyResult(object, resultBuilder);
				if (l != null) {
					result = objectMapper.writeValueAsString(l).replace("}]", "}......first " + LIMIT_SIZE + " of " + resultBuilder + " ]");
				} else {
					result = objectMapper.writeValueAsString(object);
				}
			} catch (JsonProcessingException e) {
				LOGGER.error(e.getMessage());
			}
		}
		return result;
	}
	
	public String formatDate() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}
	
	/**
	 * 在控制台输出方法的返回结果
	 *
	 * @param logger
	 * 		日志记录对象
	 * @param print
	 * 		是否打印
	 * @param result
	 * 		方法的返回结果
	 */
	public void printResult(Logger logger, boolean print, Object result) {
		if (print) {
			logger.warn(LOG_FORMAT, RESULT, toJsonString(result));
		}
	}
	
	/**
	 * 在控制台输出方法参数
	 *
	 * @param point
	 * 		切点
	 * @param logger
	 * 		日志记录对象
	 * @param print
	 * 		是否打印
	 */
	public void printParams(ProceedingJoinPoint point, Logger logger, boolean print) {
		if (print) {
			Object[] args = point.getArgs();
			if (args.length == 0) {
				return;
			}
			logger.info(LOG_FORMAT, PARAMS, toJsonString(args));
		}
	}
	
	/**
	 * 如果有@SysLogger注解采取的动作
	 *
	 * @param point
	 * 		切点
	 * @return 方法返回的结果
	 */
	@Around(value = "@within(priv.xiaohu.common.utils.logger.annotation.SysLogger) || @annotation(priv.xiaohu.common.utils.logger.annotation.SysLogger)")
	public Object logger(ProceedingJoinPoint point) throws Throwable {
		final long begin = System.currentTimeMillis();
		Class targetClass = point.getTarget().getClass();
		LOGGER = LoggerFactory.getLogger(targetClass);
		Signature signature = point.getSignature();
		boolean print = isPrint(targetClass, signature.toLongString());
		System.out.println();
		LOGGER.info(LOG_FORMAT, BEGIN, signature);
		printParams(point, LOGGER, print);
		Object result;
		try {
			result = point.proceed();
		} catch (Throwable throwable) {
			throwable.printStackTrace();
			LOGGER.error(throwable.toString());
			throw throwable;
		}
		LOGGER.info(LOG_FORMAT, END, signature);
		printResult(LOGGER, print, result);
		final long end = System.currentTimeMillis();
		LOGGER.info(LOG_FORMAT, TIME, (end - begin) + "ms");
		System.out.println();
		return result;
	}
	
	/**
	 * 返回注解的print值
	 *
	 * @param targetClass
	 * 		SysLogger标注的目标类或方法所在的类
	 * @param longMethodSignature
	 * 		方法声明
	 * @return 是否打印
	 */
	public boolean isPrint(Class targetClass, String longMethodSignature) {
		return targetClass.isAnnotationPresent(SysLogger.class)
				&& ((SysLogger) targetClass.getDeclaredAnnotation(SysLogger.class)).print()
				|| Arrays.stream(targetClass.getDeclaredMethods())
				.filter(method -> method.toString().equals(longMethodSignature)
						&& method.isAnnotationPresent(SysLogger.class))
				.anyMatch(method -> method.getAnnotation(SysLogger.class).print());
	}
	
	/**
	 * 简化 List<T> 日志输出信息
	 *
	 * @param object
	 * 		方法实际返回结果
	 * @param x
	 * 		拼接输出结果
	 * @return 简化后的返回结果
	 */
	private static Object simplifyResult(Object object, StringBuffer x) {
		
		Object r = object;
		Object d = null;
		List<?> l = null;
		Map<?, ?> map = null;
		
		try {
			Class cr = Class.forName(RESPONSE_MESSAGE);
			Class cd = Class.forName(RESPONSE_DATA);
			
			Constructor cnt_r = cr.getDeclaredConstructor(Boolean.class, String.class, String.class);
			Method mr = cr.getDeclaredMethod("getData");
			Method mr0 = cr.getDeclaredMethod("getSuccess");
			Method mr1 = cr.getDeclaredMethod("getStatus");
			Method mr2 = cr.getDeclaredMethod("getMsg");
			Method mr3 = cr.getDeclaredMethod("setData", Object.class);
			
			Constructor cnt_d = cd.getDeclaredConstructor(int.class, int.class, int.class, Integer.TYPE, List.class);
			Method ml = cd.getDeclaredMethod("getList");
			Method ml0 = cd.getDeclaredMethod("getPageSize");
			Method ml1 = cd.getDeclaredMethod("getPageNumber");
			Method ml2 = cd.getDeclaredMethod("getTotalPage");
			Method ml3 = cd.getDeclaredMethod("getTotalRow");
			
			if (r.getClass().equals(cr)) {
				d = mr.invoke(r);
				if (d == null) {
					return null;
				}
				if (cd.equals(d.getClass())) {
					l = (List) ml.invoke(d);
				} else if (d instanceof List) {
					l = (List<?>) d;
					d = null;
				}
			} else if (r instanceof List) {
				l = (List) r;
				r = null;
				d = null;
			} else if (r instanceof Map) {
				map = (Map) r;
				r = null;
				d = null;
			}
			if (map != null) {
				x.append(map.size());
				HashMap m = new HashMap(map.size());
				int i = 0;
				for (Object key : map.keySet()) {
					m.put(key, map.get(key));
					if (++i < LIMIT_SIZE) {
						break;
					}
				}
				r = m;
			} else if (l != null && l.size() > LIMIT_SIZE) {
				x.append(l.size());
				
				List li = new ArrayList();
				for (int i = 0; i < LIMIT_SIZE; i++) {
					li.add(l.get(i));
				}
				l = li;
				
				if (r == null) {
					r = l;
				} else if (d == null) {
					r = cnt_r.newInstance(mr0.invoke(r), mr1.invoke(r), mr2.invoke(r));
					mr3.invoke(r, l);
					
				} else {
					d = cnt_d.newInstance(ml0.invoke(d), ml1.invoke(d), ml2.invoke(d), ml3.invoke(d), l);
					r = cnt_r.newInstance(mr0.invoke(r), mr1.invoke(r), mr2.invoke(r));
					mr3.invoke(r, d);
				}
			}
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}
		return r;
	}
	
	/**
	 * 处理集合类的数据
	 *
	 * @param data
	 * 		集合
	 * @param resultBuilder
	 * 		输出条数
	 * @return 处理后的结果
	 */
	private Object dataHandler(Object data, final StringBuilder resultBuilder) {
		if (data instanceof Collection) {
			final Collection collection = (Collection) data;
			resultBuilder.append(collection.size());
			data = collection.stream().limit(LIMIT_SIZE).collect(Collectors.toList());
		} else if (data instanceof Map) {
			final Map map = (Map) data;
			resultBuilder.append(map.size());
			data = ((Set<Map.Entry>) map.entrySet()).stream().limit(LIMIT_SIZE).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
		return data;
	}
}
