package com.brennaswitzer.cookbook.payload;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Setter
@Getter
@SuppressWarnings("WeakerAccess")
public class SubtaskIds {

    private long[] subtaskIds;

    public SubtaskIds() {
        setSubtaskIds(new long[0]);
    }

    public SubtaskIds(long... ids) {
        setSubtaskIds(Arrays.copyOf(ids, ids.length));
    }

    public SubtaskIds(Iterable<Long> ids) {
        List<Long> idList;
        if (ids instanceof List) {
            idList = (List<Long>) ids;
        } else {
            idList = new LinkedList<>();
            for (Long id : ids) {
                idList.add(id);
            }
        }
        long[] arr = new long[idList.size()];
        int i = 0;
        for (Long id : idList) {
            arr[i++] = id;
        }
        setSubtaskIds(arr);
    }

}
