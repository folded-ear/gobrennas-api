package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.measure.UnitOfMeasure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

@Service
@Transactional
public class UnitLoader {

    @Autowired
    private EntityManager entityManager;

    public static class UomInfo {
        private String name;
        private String pluralName;
        private String[] aliases;
        private HashMap<String, Float> conversions;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPluralName() {
            if (pluralName == null) return name + "s";
            return pluralName;
        }

        public void setPluralName(String pluralName) {
            this.pluralName = pluralName;
        }

        public String[] getAliases() {
            return aliases;
        }

        public void setAliases(String[] aliases) {
            this.aliases = aliases;
        }

        public HashMap<String, Float> getConversions() {
            return conversions;
        }

        public void setConversions(HashMap<String, Float> conversions) {
            this.conversions = conversions;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("UomInfo{");
            sb.append("name='").append(name).append('\'');
            sb.append("pluralName=");
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

    @EventListener
    public void onStart(ApplicationStartedEvent e) {
        loadUnits("units.yml");
    }

    protected Collection<UnitOfMeasure> loadUnits(String resourceName) {
        return loadUnits(this.getClass()
                        .getClassLoader()
                        .getResourceAsStream(resourceName));
    }

    protected Collection<UnitOfMeasure> loadUnits(InputStream inputStream) {
        return loadUnits(new InputStreamReader(inputStream));
    }

    protected Collection<UnitOfMeasure> loadUnits(Reader reader) {
        Yaml yaml = new Yaml(new Constructor(UomInfo.class));
        Iterable<Object> infos = yaml.loadAll(reader);
        Map<String, UnitOfMeasure> unitMap = new HashMap<>();
        TypedQuery<UnitOfMeasure> unitByName = null;
        if (entityManager != null) {
            unitByName = entityManager.createQuery(
                    "select u\n" +
                            "from UnitOfMeasure u\n" +
                            "where name = ?1", UnitOfMeasure.class);

        }
        for (Object o : infos) {
            UomInfo info = (UomInfo) o;
            List<UnitOfMeasure> us;
            if (unitByName != null) {
                us = unitByName
                        .setParameter(1, info.name)
                        .getResultList();
            } else {
                us = Collections.EMPTY_LIST;
            }
            UnitOfMeasure uom;
            if (us.isEmpty()) {
                uom = new UnitOfMeasure(info.name);
                if (entityManager != null) entityManager.persist(uom);
            } else { // there's a unique key preventing more than one result
                uom = us.get(0);
            }
            if (info.aliases != null) {
                uom.addAliases(info.aliases);
            }
            unitMap.put(uom.getName(), uom);
            if (info.conversions != null) {
                info.conversions.forEach((u, f) ->
                        uom.addConversion(unitMap.get(u), f));
            }
        }
        return unitMap.values();
    }

}
