package com.brennaswitzer.cookbook.domain;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.reflections.Reflections;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentifiedEnumTest {

    private static class ToCheck implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return new Reflections("com.brennaswitzer.cookbook")
                    .getSubTypesOf(Identified.class)
                    .stream()
                    .filter(Class::isEnum)
                    .map(Arguments::of);
        }

    }

    @ParameterizedTest
    @ArgumentsSource(ToCheck.class)
    public <T extends Identified> void enumIdsAreUnique(Class<T> clazz) {
        Set<Long> ids = new HashSet<>();
        for (var v : clazz.getEnumConstants()) {
            Long id = v.getId();
            assertTrue(ids.add(id), () -> String.format(
                    "%s.%s has duplicate id: %d",
                    clazz.getSimpleName(),
                    v,
                    id));
        }
    }

}
