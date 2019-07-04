package com.yorma.common.utils.logger.interceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yorma.common.utils.logger.annotation.SysLogger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * 拦截方法,判断是否有@SysLogger注解并采取动作
 *
 * @author zxh
 * @version 1.0.0
 * @date 2019/03/18
 * @since 1.0.1
 */
@Aspect
@Component
public class LoggerInterceptor {
	public static final String LOG_FORMAT = "[{}]->方法{}: {}";
	public static final String BEGIN = "开始执行";
	public static final String END = "执行结束";
	public static final String PARAMS = "参数列表";
	public static final String RESULT = "执行结果";
	public static final String TIME = "执行用时";
	public static final String CURRENT_THREAD = "当前线程";
	public static Logger LOGGER;

	private static String toJsonString(Object object) {
		final ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			LOGGER.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public static String formatDate() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}

	/**
	 * 在控制台输出方法的返回结果
	 *
	 * @param logger
	 *            日志记录对象
	 * @param print
	 *            是否打印
	 * @param result
	 *            方法的返回结果
	 */
	public static void printResult(Logger logger, boolean print, Object result) {
		if (print) {
			logger.warn(LOG_FORMAT, formatDate(), RESULT, toJsonString(result));
		}
	}

	/**
	 * 在控制台输出方法参数
	 *  @param params
	 *            参数
	 * @param logger
	 *            日志记录对象
	 * @param print 是否打印
	 */
	public static void printParams(Object[] params, Logger logger, boolean print) {
		if (print) {
			if (params.length == 0) {
				return;
			}
			logger.info(LOG_FORMAT, formatDate(), PARAMS, toJsonString(params));
		}
	}

	/**
	 * 如果有@SysLogger注解采取的动作
	 *
	 * @param point
	 *            切点
	 * @return 方法返回的结果
	 */
	@Around(value = "@within(com.yorma.common.utils.logger.annotation.SysLogger) || @annotation(com.yorma.common.utils.logger.annotation.SysLogger)")
	public Object logger(ProceedingJoinPoint point) throws Throwable {
		// 获取拦截到的具体类
		final Class targetClass = point.getTarget().getClass();
		// 获取当前类的logger对象
		LOGGER = LoggerFactory.getLogger(targetClass);
		// 方法签名
		final Signature signature = point.getSignature();
		// 判断改方法是否需要打印参数和返回结果
		final boolean print = isPrint(targetClass, signature.toLongString());
		System.out.println();
		// 记录当前线程
		LOGGER.info(LOG_FORMAT, formatDate(), CURRENT_THREAD, Thread.currentThread().getName());
		// 记录方法开始执行
		LOGGER.info(LOG_FORMAT, formatDate(), BEGIN, signature);
		// 记录方法参数
		printParams(point.getArgs(), LOGGER, print);
		// 开始执行方法
		final Object result;
		// 记录方法开始执行的时间
		final long begin = System.currentTimeMillis();
		try {
			result = point.proceed();
		} catch (Throwable throwable) {
			// 记录方法执行时抛出的异常
			final StackTraceElement[] stackTrace = throwable.getStackTrace();
			for (StackTraceElement stackTraceElement : stackTrace) {
				LOGGER.error(stackTraceElement.toString());
			}
			throw throwable;
		}
		// 记录方法执行结束的时间
		final long end = System.currentTimeMillis();
		// 记录方法执行结束
		LOGGER.info(LOG_FORMAT, formatDate(), END, signature);
		// 记录方法返回结果
		printResult(LOGGER, print, result);
		// 记录方法执行用时
		LOGGER.info(LOG_FORMAT, formatDate(), TIME, (end - begin) + "ms");
		System.out.println();
		return result;
	}

	/**
	 * 返回注解的print值
	 *
	 * @param targetClass
	 *            SysLogger标注的目标类或方法所在的类
	 * @param longMethodSignature
	 *            方法声明
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
}
