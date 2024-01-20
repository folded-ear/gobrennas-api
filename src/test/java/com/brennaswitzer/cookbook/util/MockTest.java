package com.brennaswitzer.cookbook.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.spy;

/**
 * I will autowire-by-name any {@link Mock @Mock} collaborators defined on the
 * test class into the {@link MockTestTarget @MockTestTarget} defined on the
 * test class. The latter will be initialized via its class's default
 * constructor if its value is null. It will be wrapped with as a Mockito
 * {@link org.mockito.Mockito#spy}, whether pre- or implicitly initialized.
 *
 * <pre>{@code
 * @MockTestTarget
 * private PlanResolver resolver;
 * @Mock
 * private PlanService planService;
 * }</pre>
 *
 * <p>The {@code resolver} field will be initialized via {@code PlanResolver}'s
 * default constructor. The mocked {@code PlanService} will be injected into
 * {@code resolver}'s {@code planService} field. The resolver will be wrapped
 * with a {@code spy()}.
 *
 * <p>The initialization and wrapping of the target happens per <em>test</em>,
 * not per class. If you want to initialize it yourself, you must override
 * {@link #setup()} and initialize it before the {@code super.setup()} call, not
 * in the field declaration itself.
 */
public abstract class MockTest {

    private Field cutField;

    private Collection<Field> collabFields;

    private AutoCloseable closeable;

    @BeforeEach
    public void setup() {
        quietly(this::setupInternal);
    }

    @SneakyThrows
    private void setupInternal() {
        closeable = MockitoAnnotations.openMocks(this);
        if (cutField == null) {
            for (Field f : getClass().getDeclaredFields()) {
                if (f.isAnnotationPresent(MockTestTarget.class)) {
                    cutField = f;
                    break;
                }
            }
        }
        if (cutField == null) return;
        Object cut = ReflectionTestUtils.getField(this, cutField.getName());
        if (cut == null) {
            cut = cutField.getType().getDeclaredConstructor().newInstance();
        }
        if (collabFields == null) {
            List<Field> cfs = new ArrayList<>();
            Map<String, Field> byName = new HashMap<>();
            for (Field f : cut.getClass().getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                if (!f.isAnnotationPresent(Autowired.class)) continue;
                byName.put(f.getName(), f);
            }
            for (Field f : getClass().getDeclaredFields()) {
                if (!f.isAnnotationPresent(Mock.class)) continue;
                Field cf = byName.get(f.getName());
                if (cf == null) continue;
                cfs.add(cf);
            }
            collabFields = cfs;
        }
        for (Field cf : collabFields) {
            Object m = ReflectionTestUtils.getField(this, cf.getName());
            ReflectionTestUtils.setField(cut, cf.getName(), m);
        }
        ReflectionTestUtils.setField(this,
                                     cutField.getName(),
                                     spy(cut));
    }

    @AfterEach
    public void teardown() {
        quietly(this::teardownInternal);
    }

    @SneakyThrows
    private void teardownInternal() {
        closeable.close();
        if (cutField != null) {
            ReflectionTestUtils.setField(this, cutField.getName(), null);
        }
    }

    private void quietly(Runnable work) {
        Logger logger = ((Logger) LoggerFactory.getLogger(ReflectionTestUtils.class));
        Level level = logger.getLevel();
        logger.setLevel(Level.INFO);
        try {
            work.run();
        } finally {
            logger.setLevel(level);
        }
    }

}
