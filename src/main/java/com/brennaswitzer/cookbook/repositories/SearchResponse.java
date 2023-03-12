package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.repositories.impl.SearchResponseImpl;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public interface SearchResponse<R> {

    List<R> getContent();

    default int getSize() {
        return getContent().size();
    }

    SearchRequest getRequest();

    default int getOffset() {
        return getRequest().getOffset();
    }

    @SuppressWarnings("unused")
    default int getLimit() {
        return getRequest().getLimit();
    }

    @SuppressWarnings("unused")
    default Sort getSort() {
        return getRequest().getSort();
    }

    boolean isLast();

    default boolean isFirst() {
        return getOffset() == 0;
    }

    default boolean hasPrevious() {
        return !isFirst();
    }

    default boolean hasNext() {
        return !isLast();
    }

    default <S> SearchResponse<S> map(Function<? super R, ? extends S> converter) {
        return SearchResponseImpl.<S>builder()
                .content(getContent().stream()
                                 .map(converter)
                                 .collect(toList()))
                .request(getRequest())
                .last(isLast())
                .build();
    }

    static <R> SearchResponse<R> of(SearchRequest request,
                                    List<R> contentPlusOne) {
        boolean hasNext = contentPlusOne.size() > request.getLimit();
        return SearchResponseImpl.<R>builder()
                .content(hasNext
                                 ? contentPlusOne.subList(0, request.getLimit())
                                 : contentPlusOne)
                .request(request)
                .last(!hasNext)
                .build();
    }

}
