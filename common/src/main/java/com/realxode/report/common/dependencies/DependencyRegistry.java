package com.realxode.report.common.dependencies;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.JsonElement;
import com.realxode.report.common.dependencies.relocation.Relocation;
import com.realxode.report.common.dependencies.relocation.RelocationHandler;
import com.realxode.report.common.plugin.KumaPlugin;
import com.realxode.report.common.storage.StorageType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.realxode.report.common.dependencies.Dependency.*;
import static com.realxode.report.common.storage.StorageType.*;

public class DependencyRegistry {
    
    private static final ListMultimap<StorageType, Dependency> STORAGE_DEPENDENCIES = ImmutableListMultimap.<StorageType, Dependency>builder()
            .putAll(YAML, CONFIGURATE_CORE, CONFIGURATE_YAML)
            .putAll(JSON, CONFIGURATE_CORE, CONFIGURATE_GSON)
            .putAll(HOCON, HOCON_CONFIG, CONFIGURATE_CORE, CONFIGURATE_HOCON)
            .putAll(TOML, TOML4J, CONFIGURATE_CORE, CONFIGURATE_TOML)
            .putAll(MONGODB, MONGODB_DRIVER)
            .putAll(MARIADB, MARIADB_DRIVER, SLF4J_API, SLF4J_SIMPLE, HIKARI)
            .putAll(MYSQL, MYSQL_DRIVER, SLF4J_API, SLF4J_SIMPLE, HIKARI)
            .putAll(POSTGRESQL, POSTGRESQL_DRIVER, SLF4J_API, SLF4J_SIMPLE, HIKARI)
            .putAll(SQLITE, SQLITE_DRIVER)
            .putAll(H2, H2_DRIVER)
            .build();

    private final KumaPlugin plugin;

    public DependencyRegistry(KumaPlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean isGsonRelocated() {
        return JsonElement.class.getName().startsWith("xyz.zyran");
    }

    private static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean slf4jPresent() {
        return classExists("org.slf4j.Logger") && classExists("org.slf4j.LoggerFactory");
    }

    public Set<Dependency> resolveStorageDependencies(@NotNull Set<StorageType> storageTypes) {
        Set<Dependency> dependencies = new LinkedHashSet<>();
        for (StorageType storageType : storageTypes) {
            dependencies.addAll(STORAGE_DEPENDENCIES.get(storageType));
        }

        /*if (this.plugin.getConfiguration().get(ConfigKeys.REDIS_ENABLED)) {
            dependencies.add(COMMONS_POOL_2);
            dependencies.add(JEDIS);
            dependencies.add(SLF4J_API);
            dependencies.add(SLF4J_SIMPLE);
        }

        if (this.plugin.getConfiguration().get(ConfigKeys.RABBITMQ_ENABLED)) {
            dependencies.add(RABBITMQ);
        }

        if ((dependencies.contains(SLF4J_API) || dependencies.contains(SLF4J_SIMPLE)) && slf4jPresent()) {
            dependencies.remove(SLF4J_API);
            dependencies.remove(SLF4J_SIMPLE);
        }*/

        return dependencies;
    }

    public void applyRelocationSettings(Dependency dependency, List<Relocation> relocations) {
        if (!RelocationHandler.DEPENDENCIES.contains(dependency) && isGsonRelocated()) {
            relocations.add(Relocation.of("guava", "com{}google{}common"));
            relocations.add(Relocation.of("gson", "com{}google{}gson"));
        }
    }

    public boolean shouldAutoLoad(@NotNull Dependency dependency) {
        switch (dependency) {
            case ASM:
            case ASM_COMMONS:
            case JAR_RELOCATOR:
            case H2_DRIVER:
            case SQLITE_DRIVER:
                return false;
            default:
                return true;
        }
    }
}
