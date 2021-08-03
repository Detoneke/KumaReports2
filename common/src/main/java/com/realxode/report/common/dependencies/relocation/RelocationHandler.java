package com.realxode.report.common.dependencies.relocation;

import com.realxode.report.common.dependencies.Dependency;
import com.realxode.report.common.dependencies.DependencyManager;
import com.realxode.report.common.dependencies.classloader.IsolatedClassLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;

import static com.realxode.report.common.dependencies.Dependency.*;

public class RelocationHandler {

    public static final Set<Dependency> DEPENDENCIES = EnumSet.of(ASM, ASM_COMMONS, JAR_RELOCATOR);
    private final Constructor<?> jarRelocatorConstructor;
    private final Method jarRelocatorRunMethod;

    public RelocationHandler(DependencyManager dependencyManager) {
        try {
            dependencyManager.loadDependencies(DEPENDENCIES);
            IsolatedClassLoader classLoader = dependencyManager.obtainClassLoaderWith(DEPENDENCIES);
            String JAR_RELOCATOR_CLASS = "me.lucko.jarrelocator.JarRelocator";
            Class<?> jarRelocatorClass = classLoader.loadClass(JAR_RELOCATOR_CLASS);
            this.jarRelocatorConstructor = jarRelocatorClass.getDeclaredConstructor(File.class, File.class, Map.class);
            this.jarRelocatorConstructor.setAccessible(true);
            String JAR_RELOCATOR_RUN_METHOD = "run";
            this.jarRelocatorRunMethod = jarRelocatorClass.getDeclaredMethod(JAR_RELOCATOR_RUN_METHOD);
            this.jarRelocatorRunMethod.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void remap(Path input, Path output, @NotNull List<Relocation> relocations) throws Exception {
        Map<String, String> mappings = new HashMap<>();
        for (Relocation relocation : relocations) {
            mappings.put(relocation.getPattern(), relocation.getRelocatedPattern());
        }
        Object relocator = this.jarRelocatorConstructor.newInstance(input.toFile(), output.toFile(), mappings);
        this.jarRelocatorRunMethod.invoke(relocator);
    }
}