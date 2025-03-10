package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.repositories.UserDeviceRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.CallbackPreferringPlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
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

    // callback preference makes mocking easier
    @Mock
    private CallbackPreferringPlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        ensureUserDevice.setTransactionManager(transactionManager);
    }

    @Test
    void ensureNull_principal() {
        UserPrincipal principal = mock(UserPrincipal.class);

        ensureUserDevice.ensure(principal, null);

        verifyNoInteractions(userRepo);
        verifyNoInteractions(userDeviceRepo);
        verifyNoInteractions(transactionManager);
    }

    @Test
    void ensureNull_user() {
        User user = mock(User.class);

        ensureUserDevice.ensure(user, null);

        verifyNoInteractions(userRepo);
        verifyNoInteractions(userDeviceRepo);
        verifyNoInteractions(transactionManager);
    }

    @Test
    void ensure_principal() {
        long userId = 123L;
        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(userId);
        User user = mock(User.class);
        when(userRepo.getReferenceById(userId)).thenReturn(user);
        UserDevice device = mock(UserDevice.class);
        doReturn(device).when(ensureUserDevice).ensure(any(User.class), any());

        var result = ensureUserDevice.ensure(principal, "goat");

        assertSame(device, result);
        verify(userRepo).getReferenceById(userId);
        verify(ensureUserDevice).ensure(user, "goat");
        verifyNoInteractions(transactionManager);
    }

    @Test
    void ensure_inMemory() {
        UserDevice device = mock(UserDevice.class);
        when(device.getKey()).thenReturn("key");
        User user = mock(User.class);
        when(user.getDevices()).thenReturn(List.of(device));

        var result = ensureUserDevice.ensure(user, "key");

        assertSame(device, result);
        verifyNoInteractions(userRepo);
        verifyNoInteractions(userDeviceRepo);
        verifyNoInteractions(transactionManager);
    }

    @Test
    void ensure_notInMemory() {
        UserDevice one = mock(UserDevice.class);
        when(one.getKey()).thenReturn("one");
        UserDevice another = mock(UserDevice.class);
        User user = mock(User.class);
        when(user.getDevices()).thenReturn(List.of(one));
        when(userDeviceRepo.findByUserAndKey(any(), any()))
                .thenReturn(Optional.of(another));

        var result = ensureUserDevice.ensure(user, "another");

        assertSame(another, result);
        verifyNoInteractions(userRepo);
        verify(userDeviceRepo).findByUserAndKey(user, "another");
        verifyNoMoreInteractions(userDeviceRepo);
        verifyNoInteractions(transactionManager);
    }

    @Test
    void ensure_new() {
        long userId = 123L;
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        List<UserDevice> deviceList = new ArrayList<>();
        when(user.getDevices()).thenReturn(deviceList);
        when(userRepo.getReferenceById(userId)).thenReturn(user);
        when(userDeviceRepo.findByUserAndKey(any(), any()))
                .thenReturn(Optional.empty());
        when(userDeviceRepo.save(any()))
                .thenAnswer(iom -> iom.getArgument(0));
        when(transactionManager.execute(any(), any())).thenAnswer(iom -> {
            TransactionCallback<?> cb = iom.getArgument(1);
            return cb.doInTransaction(mock(TransactionStatus.class));
        });

        var result = ensureUserDevice.ensure(user, "another");

        assertEquals("another", result.getKey());
        assertSame(user, result.getUser());
        assertEquals("New Device", result.getName().substring(0, 10));
        verify(userRepo).getReferenceById(userId);
        verify(userDeviceRepo).findByUserAndKey(user, "another");
        ArgumentCaptor<UserDevice> captor = ArgumentCaptor.forClass(UserDevice.class);
        verify(userDeviceRepo).save(captor.capture());
        assertSame(result, captor.getValue());
        assertEquals(List.of(result), deviceList);
        verifyNoMoreInteractions(userDeviceRepo);
    }

}
