package com.yorma.common.utils.collection;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.util.Collections.sort;

/**
 * <p>双键Map集合</p>
 *
 * @param <K1> 主键
 * @param <K2> 副键
 * @param <V>  值
 * @author zxh
 */
@SuppressWarnings("ALL")
public class DoubleKeyMap<K1, K2, V> implements Serializable {
	private static final long serialVersionUID = 987749572332726095L;

	private Map<K1, Map<K2, V>> map;

    private boolean ordered;

    public DoubleKeyMap() {
        this(false);
    }

    public DoubleKeyMap(int size) {
        this(false, size);
    }

    public DoubleKeyMap(boolean ordered) {
        this.ordered = ordered;
        map = ordered ? new LinkedHashMap<>() : new HashMap<>();
    }

    public DoubleKeyMap(boolean ordered, int size) {
        this.ordered = ordered;
        map = ordered ? new LinkedHashMap<>() : new HashMap<>(size);
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
        if (map.containsKey(k1)) {
            map.get(k1).putAll(subMap);
        } else {
            map.put(k1, subMap);
        }
        return true;
    }

    public Boolean put(DoubleKeyMap<K1, K2, V> doubleKeyMap) {
        doubleKeyMap.forEach((k1, k2, v) -> {
            this.put(k1, k2, v);
        });
        return true;
    }
    
    public int size() {
        return map.values().stream().map(Map::size).reduce((size1, size2) -> size1 + size2).get();
    }
    
    public Map<K1, Map<K2, V>> toMap() {
        return map;
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

    public Boolean containsValue(V v) {
        return map.values().stream().parallel().anyMatch(value -> value.containsValue(v));
    }

    @SuppressWarnings("unchecked")
    public Boolean containsSubKey(K2 k2) {
        return map.values().stream().parallel().anyMatch(k2VMap -> k2VMap.containsKey(k2));
    }

    public List<V> getListOfAll() {
        final Collection<Map<K2, V>> values = map.values();
        List<V> list = new ArrayList<>();
        values.forEach(value -> list.addAll(value.values()));
        return list;
    }

    public List<V> getSortedListOfAll(Comparator<V> comparator) {
        final List<V> list = getListOfAll();
        sort(list, comparator);
        return list;
    }

    public List<V> getListOfSubKey(K1 k1) {
        final Map<K2, V> subMap = map.get(k1);
        final Collection<V> values = subMap.values();
        return new ArrayList<>(values);
    }

    public List<V> getSortedListOfSubKey(K1 k1, Comparator<V> comparator) {
        final List<V> list = getListOfSubKey(k1);
        sort(list, comparator);
        return list;
    }

    public void forEach(TriConsumer<K1, K2, V> consumer) {
        map.forEach((k1, subMap) -> {
            subMap.forEach((k2, v) -> {
                consumer.accept(k1, k2, v);
            });
        });
    }

    public void forEach(BiConsumer<K1, Map<K2, V>> consumer) {
        map.forEach((k1, subMap) -> {
            consumer.accept(k1, subMap);
        });
    }
    
    public Stream<Map.Entry<K1, Map<K2, V>>> stream(){
        return map.entrySet().stream();
    }
    
    public void clear(){
        map.clear();
    }
}