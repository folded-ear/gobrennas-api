package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AppSetting;
import com.brennaswitzer.cookbook.domain.DataType;
import com.brennaswitzer.cookbook.domain.UnitOfMeasure;
import com.brennaswitzer.cookbook.repositories.AppSettingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import lombok.Data;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@Profile("!test")
public class UnitLoader {

    static final String SETTING_NAME_PREFIX = "unit-loader:";
    private static final Duration LOAD_TTL = Duration.of(2, ChronoUnit.MINUTES);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AppSettingRepository appSettingRepo;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionTemplate txTemplate;

    // SnakeYaml requires this be public
    @Setter
    public static class UomInfo {

        private String name;
        private String pluralName;
        private String[] aliases;
        private HashMap<String, Double> conversions;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("UomInfo{");
            sb.append("name='").append(name).append('\'');
            sb.append(", pluralName=");
            if (pluralName == null)
                sb.append("null");
            else
                sb.append('\'').append(pluralName).append('\'');
            sb.append(", aliases=").append(Arrays.toString(aliases));
            sb.append(", conversions=").append(conversions);
            sb.append('}');
            return sb.toString();
        }

    }

    @Data
    private static class Status {

        private String sha1;
        private boolean complete;

    }

    @Data
    private class SettingAndStatus {

        final AppSetting setting;
        final Status status;
        boolean loadNeeded;

        public SettingAndStatus(String resourceName) {
            String settingName = SETTING_NAME_PREFIX + resourceName;
            setting = appSettingRepo.findByName(settingName)
                    .orElseGet(() -> new AppSetting(settingName,
                                                    DataType.JSON));
            status = getStatusFromSetting(setting);
        }

        public void setCurrentSha1(String sha1) {
            if (!Objects.equals(sha1, status.getSha1())) {
                // different resource - do it
                loadNeeded = true;
            } else if (status.isComplete()) {
                // same resource and complete - skip
                loadNeeded = false;
            } else {
                // incomplete - has it been long enough?
                loadNeeded = setting.getAgeOfValue().compareTo(LOAD_TTL) > 0;
            }
            if (loadNeeded) {
                status.setComplete(false);
                status.setSha1(sha1);
                save();
            }
        }

        public void complete() {
            status.setComplete(true);
            save();
        }

        @SneakyThrows
        private void save() {
            setting.setValue(objectMapper.writeValueAsString(status));
            appSettingRepo.save(setting);
        }

    }

    private Status getStatusFromSetting(AppSetting setting) {
        Status status = null;
        if (setting.getValue() != null) {
            try {
                status = objectMapper.readValue(setting.getValue(),
                                                Status.class);
            } catch (JsonProcessingException e) {
                log.warn("Failed to read status", e);
            }
        }
        if (status == null) {
            status = new Status();
        }
        return status;
    }

    @Transactional
    public Collection<UnitOfMeasure> loadUnits(String resourceName) {
        val cl = this.getClass().getClassLoader();
        return loadUnits(
                resourceName,
                () -> cl.getResourceAsStream(resourceName));
    }

    @SneakyThrows
    protected Collection<UnitOfMeasure> loadUnits(String resourceName,
                                                  InputStreamSource streamSource) {
        String sha1 = DigestUtils.sha1Hex(streamSource.getInputStream());
        SettingAndStatus settingAndStatus = inTxWithRetry(tx -> {
            SettingAndStatus ss = new SettingAndStatus(resourceName);
            ss.setCurrentSha1(sha1);
            return ss;
        });
        if (settingAndStatus.isLoadNeeded()) {
            var units = inTxWithRetry(tx -> {
                var us = loadUnitsInternal(streamSource);
                settingAndStatus.complete();
                return us;
            });
            log.info("loaded {} units from '{}'", units.size(), resourceName);
            return units;
        }
        log.info("skip loading units from '{}'", resourceName);
        return Collections.emptySet();
    }

    private <T> T inTxWithRetry(TransactionCallback<T> action) {
        RuntimeException suppressed = null;
        for (int i = 0; i < 2; i++) {
            try {
                return txTemplate.execute(action);
            } catch (PersistenceException | TransactionException e) {
                if (suppressed != null) {
                    e.addSuppressed(suppressed);
                }
                suppressed = e;
            }
        }
        throw suppressed;
    }

    @SneakyThrows
    private Collection<UnitOfMeasure> loadUnitsInternal(InputStreamSource streamSource) {
        Yaml yaml = new Yaml(new Constructor(UomInfo.class,
                                             new LoaderOptions()));
        Iterable<?> infos = yaml.loadAll(streamSource.getInputStream());
        Map<String, UnitOfMeasure> unitMap = new HashMap<>();
        for (Object o : infos) {
            UomInfo info = (UomInfo) o;
            UnitOfMeasure uom = ensure(info.name);
            if (info.pluralName != null) {
                uom.setPluralName(info.pluralName);
            }
            if (info.aliases != null) {
                uom.addAliases(info.aliases);
            }
            if (!info.name.equals(info.name.toLowerCase())) {
                uom.addAlias(info.name.toLowerCase());
            }
            unitMap.put(info.name, uom);
            if (info.conversions != null) {
                info.conversions.forEach((u, f) -> {
                    if (!unitMap.containsKey(u)) {
                        throw new IllegalArgumentException("No '" + u + "' unit is available (converting from '" + info.name + "')");
                    }
                    uom.addConversion(unitMap.get(u), f);
                });
            }
        }
        return unitMap.values();
    }

    @VisibleForTesting
    UnitOfMeasure ensure(String name) {
        return entityManager == null
                ? new UnitOfMeasure(name)
                : UnitOfMeasure.ensure(entityManager, name);
    }

}
