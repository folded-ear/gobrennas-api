package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.repositories.UserDeviceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserDeviceTest {

    @InjectMocks
    private GetUserDevice getDevice;

    @Mock
    private UserDeviceRepository userDeviceRepo;

    @Test
    void happyPath() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(123L);
        UserDevice device = mock(UserDevice.class);
        when(device.getUser()).thenReturn(user);
        when(userDeviceRepo.getReferenceById(456L))
                .thenReturn(device);

        UserDevice d = getDevice.get(123L, 456L);

        assertSame(device, d);
    }

    @Test
    void invalidDevice() {
        UserDevice device = mock(UserDevice.class);
        when(device.getUser()).thenThrow(new EntityNotFoundException());
        when(userDeviceRepo.getReferenceById(456L))
                .thenReturn(device);

        assertThrows(EntityNotFoundException.class,
                     () -> getDevice.get(123L, 456L));
    }

    @Test
    void mismatchedOwner() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        UserDevice device = mock(UserDevice.class);
        when(device.getUser()).thenReturn(user);
        when(userDeviceRepo.getReferenceById(123L))
                .thenReturn(device);

        assertThrows(EntityNotFoundException.class,
                     () -> getDevice.get(666L, 123L));
    }

}
