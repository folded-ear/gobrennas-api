package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Plan;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PlanServiceMockedTest {

    @InjectMocks
    @Spy
    private PlanService service;

    @Test
    void setColor() {
        var plan = mock(Plan.class);
        doReturn(plan).when(service).getPlanById(any(), any());

        service.setColor(123L, "#F57F17");
        service.setColor(123L, "#f57f17");
        assertThrows(IllegalArgumentException.class,
                     () -> service.setColor(123L, " #F57F17"));
        assertThrows(IllegalArgumentException.class,
                     () -> service.setColor(123L, "#f57f17 "));
        assertThrows(IllegalArgumentException.class,
                     () -> service.setColor(123L, "#F00"));
        assertThrows(IllegalArgumentException.class,
                     () -> service.setColor(123L, "red"));
        service.setColor(123L, "");
        service.setColor(123L, null);

        var inOrder = inOrder(plan);
        inOrder.verify(plan).setColor("#F57F17");
        inOrder.verify(plan).setColor("#f57f17");
        inOrder.verify(plan).setColor("");
        inOrder.verify(plan).setColor(null);
        inOrder.verifyNoMoreInteractions();
        verify(service, times(4))
                .getPlanById(123L, AccessLevel.CHANGE);
    }

}
