package com.brennaswitzer.cookbook.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Slice;

import java.util.List;

@Data
@AllArgsConstructor
public class Page<E> {

    public static <E> Page<E> from(Slice<E> slice) {
        return new Page<>(
                slice.getNumber(),
                slice.getSize(),
                slice.isFirst(),
                slice.isLast(),
                slice.getContent()
        );
    }

    private int page;
    private int pageSize;
    private boolean first;
    private boolean last;
    private List<E> content;

}
