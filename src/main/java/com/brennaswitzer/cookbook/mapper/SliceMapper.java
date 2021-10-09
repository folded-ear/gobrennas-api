package com.brennaswitzer.cookbook.mapper;

import com.brennaswitzer.cookbook.payload.Page;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Slice;

import java.util.function.Function;

@Mapper
public interface SliceMapper {

    default <T> Page<T> sliceToPage(Slice<T> slice) {
        return new Page<>(
                slice.getNumber(),
                slice.getSize(),
                slice.isFirst(),
                slice.isLast(),
                slice.getContent()
        );
    }

    default <S, T> Page<T> sliceToPage(Slice<S> slice, Function<S, T> itemMapper) {
        return sliceToPage(slice.map(itemMapper));
    }

}
