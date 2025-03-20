package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.repositories.UserDeviceRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnsureUserDeviceTest {

    @InjectMocks
    @Spy
    private EnsureUserDevice ensureUserDevice;

    @Mock
    private UserRepository userRepo;

    @Mock
    private UserDeviceRepository userDeviceRepo;

    @Test
    void forReadNull_user() {
        User user = mock(User.class);

        ensureUserDevice.forRead(user, null);

        verifyNoInteractions(userRepo);
        verifyNoInteractions(userDeviceRepo);
    }

    @Test
    void forRead_exists() {
        long userId = 123456L;
        UserDevice another = mock(UserDevice.class);
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(userDeviceRepo.findByUserIdAndKey(any(), any()))
                .thenReturn(Optional.of(another));
        when(userDeviceRepo.save(any()))
                .thenAnswer(iom -> iom.getArgument(0));

        var result = ensureUserDevice.forRead(user, "another");

        assertSame(another, result);
        verifyNoInteractions(userRepo);
        verify(userDeviceRepo).findByUserIdAndKey(userId, "another");
        verifyNoMoreInteractions(userDeviceRepo);
    }

    @Test
    void forRead_new() {
        long userId = 123L;
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        List<UserDevice> deviceList = new ArrayList<>();
        when(user.getDevices()).thenReturn(deviceList);
        when(userRepo.getReferenceById(userId)).thenReturn(user);
        when(userDeviceRepo.findByUserIdAndKey(any(), any()))
                .thenReturn(Optional.empty());
        when(userDeviceRepo.save(any()))
                .thenAnswer(iom -> iom.getArgument(0));

        var result = ensureUserDevice.forRead(user, "another");

        assertEquals("another", result.getKey());
        assertSame(user, result.getUser());
        assertEquals("New Device", result.getName().substring(0, 10));
        verify(userRepo).getReferenceById(userId);
        verify(userDeviceRepo).findByUserIdAndKey(userId, "another");
        ArgumentCaptor<UserDevice> captor = ArgumentCaptor.forClass(UserDevice.class);
        verify(userDeviceRepo).save(captor.capture());
        assertSame(result, captor.getValue());
        assertEquals(List.of(result), deviceList);
        verifyNoMoreInteractions(userDeviceRepo);
    }

    @Test
    void forWrite_inMemory() {
        UserDevice device = mock(UserDevice.class);
        when(device.getKey()).thenReturn("key");
        User user = mock(User.class);
        when(user.getDevices()).thenReturn(List.of(device));

        var result = ensureUserDevice.forWrite(user, "key");

        assertSame(device, result);
        verifyNoInteractions(userRepo);
        verifyNoInteractions(userDeviceRepo);
    }

}
