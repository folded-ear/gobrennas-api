package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.UnitOfMeasure;
import com.brennaswitzer.cookbook.repositories.AppSettingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UnitLoaderTest {

    @InjectMocks
    @Spy
    private UnitLoader loader;

    @Mock
    private AppSettingRepository appSettingRepo;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private TransactionTemplate txTemplate;

    @BeforeEach
    public void setup() {
        doAnswer(iom -> new UnitOfMeasure(iom.getArgument(0)))
                .when(loader)
                .ensure(any());
        when(txTemplate.execute(any()))
                .thenAnswer(iom -> {
                    TransactionCallback<?> work = iom.getArgument(0);
                    return work.doInTransaction(mock(TransactionStatus.class));
                });
    }

    @Test
    public void loadGram() {
        Collection<UnitOfMeasure> units = loader
                .loadUnits("units/gram.yml");

        assertEquals(1, units.size());
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        var gram = units.stream()
                .filter(uom -> "gram".equals(uom.getName()))
                .findFirst()
                .get();
        assertNull(gram.getPluralName());
    }

    @Test
    public void loadImperialWeights() {
        Collection<UnitOfMeasure> units = loader
                .loadUnits("units/imperial-weights.yml");

        assertEquals(2, units.size());
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        var oz = units.stream()
                .filter(uom -> "oz".equals(uom.getName()))
                .findFirst()
                .get();
        assertEquals("oz", oz.getPluralName());
    }

}
