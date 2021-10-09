package org.zxp.ConcurrentLatch.expand.standalonemr.utils;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionUtils {
    /**
     * 根据数量拆分list
     * @param oriList
     * @param isParallel
     * @param count
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> splitListWithCount(List<T> oriList, boolean isParallel,Long count){
        if(oriList.size() <=  count){
            List<List<T>> splitList = new ArrayList<>();
            splitList.add(oriList);
            return splitList;
        }
        Long limit = (oriList.size() + count - 1) / count;
        if(isParallel){
            return Stream.iterate(0, n -> n + 1).limit(limit).parallel().map(a -> oriList.stream().skip(a * count).limit(count).parallel().collect(Collectors.toList())).collect(Collectors.toList());
        }else{
            final List<List<T>> splitList = new ArrayList<>();
            Stream.iterate(0, n -> n + 1).limit(limit).forEach(i -> {
                splitList.add(oriList.stream().skip(i * count).limit(count).collect(Collectors.toList()));
            });
            return splitList;
        }
    }

    /**
     * 根据hash算法拆分list
     * @param oriList
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> splitListWithHash(List<T> oriList,int partition){
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < partition; i++) {
            List<T> list = new ArrayList<>();
            result.add(list);
        }
        for (int i = 0; i < oriList.size(); i++) {
            int index = (i+1)%partition;
            result.get(index).add(oriList.get(i));
        }
        return result;
    }

    /**
     * 根据murmurhash算法拆分list
     * @param oriList
     * @param isParallel
     * @param count
     * @param <T>
     * @return
     */
    public static <T> List<List<T>> splitListWithMurmurHash(List<T> oriList, boolean isParallel,Long count){
        HashFunction murmur3 = Hashing.murmur3_32();
        return null;
    }
}
