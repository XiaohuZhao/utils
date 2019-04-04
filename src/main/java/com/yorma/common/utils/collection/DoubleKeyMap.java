package com.yorma.common.utils.collection;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>双键Map集合</p>
 * <p>包含put、get</p>
 * <p>获取副键和值的Map集合</p>
 * <p>全部值转成List和副键的值转成List</p>
 *
 * @param <K1> 主键
 * @param <K2> 副键
 * @param <V>  值
 * @author zxh
 */
@SuppressWarnings("ALL")
public class DoubleKeyMap<K1, K2, V> {
    private Map<K1, Map<K2, V>> map;

    private boolean ordered;

    public DoubleKeyMap() {
        ordered = false;
        map = new HashMap<>();
    }

    public DoubleKeyMap(boolean ordered) {
        this.ordered = ordered;
        map = ordered ? new LinkedHashMap<>() : new HashMap<>();
    }

    public Boolean put(K1 k1, K2 k2, V v) {
        if (map.containsKey(k1)) {
            return map.get(k1).put(k2, v) != null;
        } else {
            final Map<K2, V> subMap = ordered ? new LinkedHashMap<>() : new HashMap<>(8);
            subMap.put(k2, v);
            return map.put(k1, subMap) != null;
        }
    }

    public Boolean put(K1 k1, Map<K2, V> subMap) {
        return map.put(k1, subMap) != null;
    }

    public Map<K2, V> get(K1 k1) {
        return map.get(k1);
    }

    public V get(K1 k1, K2 k2) {
        return map.get(k1).get(k2);
    }

    public Map<K2, V> remove(K1 k1) {
        return map.remove(k1);
    }

    public V remove(K1 k1, K2 k2) {
        return map.get(k1).remove(k2);
    }

    public Boolean containsKey(K1 k1) {
        return map.containsKey(k1);
    }

    public Boolean containsKey(K1 k1, K2 k2) {
        return map.get(k1).containsKey(k2);
    }

    public boolean isOrdered() {
        return ordered;
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    public Boolean containsValue(V v) {
        final Map<K2, V> subMap = (Map<K2, V>) map.values();
        return subMap.containsValue(v);
    }

    @SuppressWarnings("unchecked")
    public Boolean containsSubKey(K2 k2) {
        AtomicReference<Boolean> containsSubKey = new AtomicReference<>(false);
        final Set<Map.Entry<K1, Map<K2, V>>> entries = map.entrySet();
        entries.forEach(entry -> {
            final Map<K2, V> subMap = (Map<K2, V>) entry;
            containsSubKey.set(subMap.containsKey(k2));
        });
        return containsSubKey.get();
    }

    public List<V> getListOfAll() {
        final Collection<Map<K2, V>> values = map.values();
        List<V> list = new ArrayList<>();
        values.forEach(value -> list.addAll(value.values()));
        return list;
    }

    public List<V> getSortedListOfAll(Comparator<V> comparator) {
        final List<V> list = getListOfAll();
        Collections.sort(list, comparator);
        return list;
    }

    public List<V> getListOfSubKey(K1 k1) {
        final Map<K2, V> subMap = map.get(k1);
        final Collection<V> values = subMap.values();
        return new ArrayList<>(values);
    }

    public List<V> getSortedListOfSubKey(K1 k1, Comparator<V> comparator) {
        final List<V> list = getListOfSubKey(k1);
        Collections.sort(list, comparator);
        return list;
    }
}