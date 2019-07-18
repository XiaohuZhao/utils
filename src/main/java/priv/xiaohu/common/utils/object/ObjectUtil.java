package priv.xiaohu.common.utils.object;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 处理对象的一些方法
 *
 * @author zxh
 * @version 1.0.0
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
	 * <p>数组长度为0</p>
	 * <p>自定义类型只能判断是否为null</p>
	 *
	 * @param t
	 * 		要判断的对象
	 * @return 是否为空
	 */
	public static boolean isEmpty(Object t) {
		if (null == t) {
			return true;
		} else if (t.getClass().isPrimitive() || t instanceof Boolean || t instanceof Number) {
			return false;
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
			return false;
		}
	}
	
	/**
	 * 判断一组数据，判断数组为空或数组中的数据至少有一个为空
	 *
	 * @param objects
	 * 		一组数据
	 * @return 是否至少有一个为空
	 */
	public static boolean isAnyEmpty(Object... objects) {
		return isAnyEmpty(Arrays.asList(objects));
	}
	
	/**
	 * 判断一组数据，判断集合为空或集合中的数据至少有一个为空
	 *
	 * @param collection
	 * 		一组数据
	 * @return 是否至少有一个为空
	 */
	public static boolean isAnyEmpty(Collection collection) {
		return isEmpty(collection) || collection.stream().anyMatch(ObjectUtil::isEmpty);
	}
	
	/**
	 * 判断一组数据，判断数组为空或所有数组中所有数据都为空
	 *
	 * @param objects
	 * 		一组数据
	 * @return 数组为空或数组中的数据都为空
	 */
	public static boolean isAllEmpty(Object... objects) {
		return isAllEmpty(Arrays.asList(objects));
	}
	
	/**
	 * 判断一组数据，判断数组为空或所有数组中所有数据都为空
	 *
	 * @param collection
	 * 		一组数据
	 * @return 数组为空或数组中的数据都为空
	 */
	public static boolean isAllEmpty(Collection collection) {
		return isEmpty(collection) || collection.stream().allMatch(ObjectUtil::isEmpty);
	}
	
	/**
	 * 判断一组数据，判断数组不为空或所有数组中所有数据至少有一个不为空
	 *
	 * @param objects
	 * 		一组数据
	 * @return 数组不为空或所有数组中所有数据至少有一个不为空
	 */
	public static boolean isAnyNotEmpty(Object... objects) {
		return isAnyNotEmpty(Arrays.asList(objects));
	}
	
	/**
	 * 判断一组数据，判断集合不为空或集合中所有数据至少有一个不为空
	 *
	 * @param collection
	 * 		一组数据
	 * @return 数组不为空或所有数组中所有数据至少有一个不为空
	 */
	public static boolean isAnyNotEmpty(Collection collection) {
		return isNotEmpty(collection) || collection.stream().anyMatch(ObjectUtil::isNotEmpty);
	}
	
	/**
	 * 判断一组数据，判断数组不为空或所有数组中所有数据至少有一个不为空
	 *
	 * @param objects
	 * 		一组数据
	 * @return 数组不为空或所有数组中所有数据至少有一个不为空
	 */
	public static boolean isAllNotEmpty(Object... objects) {
		return isAllNotEmpty(Arrays.asList(objects));
	}
	
	/**
	 * 判断一组数据，判断数组为空或所有数组中所有数据都为空
	 *
	 * @param collection
	 * 		一组数据
	 * @return 数组为空或数组中的数据都为空
	 */
	public static boolean isAllNotEmpty(Collection collection) {
		return isNotEmpty(collection) || collection.stream().allMatch(ObjectUtil::isNotEmpty);
	}
	
	/**
	 * <p>判断对象是否不为空</p>
	 * <p>引用指向的地址不为空</p>
	 * <p>指向的值存在</p>
	 * <p>字符串的长度大于0</p>
	 * <p>集合的长度大于0</p>
	 * <p>数组长度大于0</p>
	 * <p>自定义类型只能判断是否为null</p>
	 *
	 * @param t
	 * 		要判断的对象
	 * @return 是否不为空或长度大于0
	 */
	public static boolean isNotEmpty(Object t) {
		return !isEmpty(t);
	}
	
	/**
	 * 如果对象不为空则对对象进行处理
	 *
	 * @param t
	 * 		要判断的对象
	 * @param function
	 * 		要对非空对象进行的处理
	 * @param <T>
	 * 		源对象类型
	 * @param <R>
	 * 		处理后对象的类型
	 * @return 处理后的对象
	 */
	public static <T, R> R ifNotEmpty(T t, Function<T, R> function) {
		return isNotEmpty(t) ? function.apply(t) : null;
	}
	
	/**
	 * 如果对象不为空则对对象进行处理
	 *
	 * @param t
	 * 		要判断的对象
	 * @param consumer
	 * 		要对非空对象进行的处理
	 * @param <T>
	 * 		源对象类型
	 */
	public static <T> void ifNotEmpty(T t, Consumer<T> consumer) {
		if (isNotEmpty(t)) {
			consumer.accept(t);
		}
	}
	
	/**
	 * 如果对象为空则进行操作并返回同类型的对象
	 *
	 * @param t
	 * 		要判断的对象
	 * @param supplier
	 * 		返回同类型对象的操作
	 * @param <T>
	 * 		对象类型
	 * @return 处理后的对象
	 */
	public static <T> T ifEmpty(T t, Supplier<T> supplier) {
		return isNotEmpty(t) ? supplier.get() : t;
	}
	
	/**
	 * 将对象转成int类型的数字
	 *
	 * @param o
	 * 		要转的对象
	 * @return 对象中的数字
	 */
	public static int parseInt(Object o) {
		if (isEmpty(o)) {
			throw new NullPointerException("转换的对象不可为空");
		}
		return Integer.parseInt(o.toString());
	}
	
	/**
	 * 将对象转成double类型的数字
	 *
	 * @param o
	 * 		要转的对象
	 * @return 对象中的数字
	 */
	public static double parseDouble(Object o) {
		if (isEmpty(o)) {
			throw new NullPointerException("转换的对象不可为空");
		}
		return Double.parseDouble(o.toString());
	}
}
