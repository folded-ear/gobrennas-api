package com.brennaswitzer.cookbook.payload;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
        List<Long> list;
        if (ids instanceof List) {
            list = (List<Long>) ids;
        } else {
            list = new LinkedList<>();
            for (Long id : ids) {
                list.add(id);
            }
        }
        long[] arr = new long[list.size()];
        int i = 0;
        for (Long id : list) {
            arr[i++] = id;
        }
        setSubtaskIds(arr);
    }

    public long[] getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtaskIds(long[] subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

}
