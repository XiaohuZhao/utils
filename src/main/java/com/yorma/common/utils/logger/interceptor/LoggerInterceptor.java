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
import java.util.Optional;

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
    private static final String LOG_FORMAT = "[AOP-INFO][{}]->Method {}: {}";
    private static final String BEGIN = "begin";
    private static final String END = "end";
    private static final String PARAMS = "params";
    private static final String RESULT = "result";
    private static Logger LOGGER;

    private static String toJsonString(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        String result = null;
        try {
            result = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
        }
        return result;
    }

    private String formatDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * 如果有@SysLogger注解采取的动作
     *
     * @param point 切点
     * @return 方法返回的结果
     * @throws Throwable 方法抛出的异常
     */
    @Around(value = "@within(com.yorma.common.utils.logger.annotation.SysLogger) || @annotation(com.yorma.common.utils.logger.annotation.SysLogger)")
    public Object test(ProceedingJoinPoint point) throws Throwable {
        Class targetClass = point.getTarget().getClass();
        LOGGER = LoggerFactory.getLogger(targetClass);
        Signature signature = point.getSignature();
        boolean print = isPrint(targetClass, signature.toLongString());
        System.out.println();
        LOGGER.info(LOG_FORMAT, formatDate(), BEGIN, signature);
        printParams(point, LOGGER, print);
        Object result = point.proceed();
        printResult(LOGGER, print, result);
        LOGGER.info(LOG_FORMAT, formatDate(), END, signature);
        System.out.println();
        return result;
    }

    /**
     * 在控制台输出方法的返回结果
     *
     * @param logger 日志记录对象
     * @param print  是否打印
     * @param result 方法的返回结果
     */
    private void printResult(Logger logger, boolean print, Object result) {
        if (print) {
            Optional.ofNullable(result)
                    .ifPresent(o -> logger.info(LOG_FORMAT, formatDate(), RESULT, o));
        }
    }

    /**
     * 在控制台输出方法参数
     *
     * @param point  切点
     * @param logger 日志记录对象
     * @param print  是否打印
     */
    private void printParams(ProceedingJoinPoint point, Logger logger, boolean print) {
        if (print) {
            Object[] args = point.getArgs();
            if (args.length == 0) {
                return;
            }
            logger.info(LOG_FORMAT, formatDate(), PARAMS, toJsonString(args));
        }
    }

    /**
     * 返回注解的print值
     *
     * @param targetClass         SysLogger标注的目标类或方法所在的类
     * @param longMethodSignature 方法声明
     * @return 是否打印
     */
    private boolean isPrint(Class targetClass, String longMethodSignature) {
        return targetClass.isAnnotationPresent(SysLogger.class)
                && ((SysLogger) targetClass.getDeclaredAnnotation(SysLogger.class)).print()
                || Arrays.stream(targetClass.getDeclaredMethods())
                .filter(method -> method.toString().equals(longMethodSignature) && method.isAnnotationPresent(SysLogger.class))
                .anyMatch(method -> method.getAnnotation(SysLogger.class).print());
    }
}
