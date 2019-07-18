package priv.xiaohu.common.utils.logger.annotation;

import java.lang.annotation.*;

/**
 * <p>使用注解控制日志打印</p>
 * <p>在需要打印日志的类或方法上标记此注解</p>
 *
 * @author zxh
 * @version 1.0.0
 * @date 2019/03/18
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SysLogger {
    // 控制是否打印参数和返回结果, 默认打印
    boolean print() default true;
}
