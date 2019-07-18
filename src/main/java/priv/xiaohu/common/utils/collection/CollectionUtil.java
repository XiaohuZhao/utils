package priv.xiaohu.common.utils.collection;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 集合工具类
 *
 * @author zxh
 */
public class CollectionUtil {

    /**
     * 将List集合按照每size个分一组
     *
     * @param list 要分隔的List集合
     * @return 分割后的集合的List集合
     */
    public static <T> List<Collection<T>> splitEntities(Collection<T> list, int size) {
        // 计算组数
        int limit = (list.size() + size - 1) / size;
        return Stream.iterate(0, n -> n + 1)
                .limit(limit).parallel()
                .map(a -> list.stream().skip(a * size).limit(size).parallel().collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    /**
     * 将Map集合按照每size个分一组
     *
     * @param map 要分隔的实体map集合
     * @return 分割后的Map集合的List集合
     */
    public static <K, V> List<Map<K, V>> splitEntities(Map<K, V> map, int size) {
        // 计算组数
        int limit = (map.size() + size - 1) / size;
        return Stream.iterate(0, n -> n + 1)
                .limit(limit).parallel()
                .map(a -> map.entrySet().stream().skip(a * size).limit(size).parallel().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                .collect(Collectors.toList());
    }
}
