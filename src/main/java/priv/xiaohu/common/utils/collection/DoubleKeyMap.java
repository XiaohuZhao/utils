package priv.xiaohu.common.utils.collection;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
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
public class DoubleKeyMap<K1, K2, V> implements Serializable, Iterable<Map.Entry<K1, Map<K2, V>>>, Cloneable {
	private static final long serialVersionUID = 987749572332726095L;

	private Map<K1, Map<K2, V>> map;

    private boolean ordered;
    private final static int DEFUALT_SIZE = 1 << 4;

    public DoubleKeyMap() {
        this(false);
    }

    public DoubleKeyMap(int size) {
        this(false, size);
    }

    public DoubleKeyMap(boolean ordered) {
        this(ordered, DEFUALT_SIZE);
    }

    public DoubleKeyMap(boolean ordered, int size) {
        this.ordered = ordered;
        map = ordered ? new LinkedHashMap<>() : new HashMap<>(size);
    }

    public Boolean put(K1 k1, K2 k2, V v) {
        if (map.containsKey(k1) && map.get(k1) != null) {
            return map.get(k1).put(k2, v) == v;
        } else {
            final Map<K2, V> subMap = ordered ? new LinkedHashMap<>() : new HashMap<>(DEFUALT_SIZE);
            subMap.put(k2, v);
            return map.put(k1, subMap) == subMap;
        }
    }
    
    public Boolean putIfAbsent(K1 k1, K2 k2, V v) {
        if (map.containsKey(k1) && map.get(k1).get(k2) != null) {
            return false;
        }
        return put(k1, k2, v);
    }
    
    public Boolean put(K1 k1, Map<K2, V> subMap) {
        if (map.containsKey(k1) && map.get(k1) != null) {
            map.get(k1).putAll(subMap);
        } else {
            map.put(k1, subMap);
        }
        return true;
    }
    
    public Boolean put(DoubleKeyMap<K1, K2, V> that) {
        that.forEach((k1, k2, v) -> {
            this.put(k1, k2, v);
        });
        return true;
    }
    
    public Boolean putIfAbsent(DoubleKeyMap<K1, K2, V> that) {
        that.forEach((k1, k2, v) -> {
            this.putIfAbsent(k1, k2, v);
        });
        return true;
    }
    
    public int size() {
        return map.size();
    }
    
    public Map<K1, Set<K2>> keys() {
        return map.keySet().stream().collect(Collectors.toMap(k1 -> k1, k1 -> map.get(k1).keySet()));
    }
    
    public Set<Map.Entry<K1, Map<K2, V>>> entrySet() {
        return map.entrySet();
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
        return map.values().stream().parallel().anyMatch(subMap -> subMap.containsValue(v));
    }

    @SuppressWarnings("unchecked")
    public Boolean containsSubKey(K2 k2) {
        return map.values().stream().parallel().anyMatch(subMap -> subMap.containsKey(k2));
    }

    public List<V> getListOfAll() {
        final Collection<Map<K2, V>> values = map.values();
        List<V> list = new ArrayList<>();
        values.forEach(value -> list.addAll(value.values()));
        return Collections.unmodifiableList(list);
    }

    public List<V> getSortedListOfAll(Comparator<V> comparator) {
        final Collection<Map<K2, V>> values = map.values();
        List<V> list = new ArrayList<>();
        values.forEach(value -> list.addAll(value.values()));
        sort(list, comparator);
        return Collections.unmodifiableList(list);
    }

    public List<V> getListOfSubKey(K1 k1) {
        return Collections.unmodifiableList(new ArrayList<>(map.get(k1).values()));
    }

    public List<V> getSortedListOfSubKey(K1 k1, Comparator<V> comparator) {
        final List<V> list = new ArrayList<>(map.get(k1).values());
        sort(list, comparator);
        return Collections.unmodifiableList(list);
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
    
    @Override
    public Iterator<Map.Entry<K1, Map<K2, V>>> iterator() {
        return map.entrySet().iterator();
    }
    
    @Override
    public void forEach(final Consumer<? super Map.Entry<K1, Map<K2, V>>> action) {
        map.entrySet().forEach(entry -> action.accept(entry));
    }
    
    @Override
    public Spliterator<Map.Entry<K1, Map<K2, V>>> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), 0);
    }
    
    @Override
    public DoubleKeyMap<K1, K2, V> clone() {
        try {
            return (DoubleKeyMap<K1, K2, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public boolean equals(final Object thatObj) {
        if (thatObj == this) {
            return true;
        }
        if (thatObj instanceof DoubleKeyMap) {
            try {
                final DoubleKeyMap<K1, K2, V> that = (DoubleKeyMap<K1, K2, V>) thatObj;
                if (this.size() != that.size()) {
                    return false;
                }
                final Set<K1> thisKeys = map.keySet();
                for (K1 thisKey : thisKeys) {
                    if (!that.containsKey(thisKey)) {
                        return false;
                    } else {
                        final Map<K2, V> thatSubMap = that.get(thisKey);
                        final Map<K2, V> thisSubMap = this.get(thisKey);
                        if (thisSubMap.size() != thatSubMap.size()) {
                            return false;
                        } else {
                            final Set<K2> keySet = thisSubMap.keySet();
                            for (K2 k2 : keySet) {
                                if (!thisSubMap.containsKey(k2)) {
                                    return false;
                                } else {
                                    if (!thisSubMap.get(k2).equals(thatSubMap.get(k2))) {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (ClassCastException e) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }
}