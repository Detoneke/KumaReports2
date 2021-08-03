package com.realxode.report.common.dependencies;

import com.google.common.collect.ImmutableSet;
import com.realxode.report.common.dependencies.classloader.IsolatedClassLoader;
import com.realxode.report.common.dependencies.relocation.Relocation;
import com.realxode.report.common.dependencies.relocation.RelocationHandler;
import com.realxode.report.common.plugin.KumaPlugin;
import com.realxode.report.common.storage.StorageType;
import com.realxode.report.common.util.MoreFiles;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class DependencyManager {

    private final KumaPlugin plugin;
    private final DependencyRegistry registry;
    private final Path cacheDirectory;
    private final Map<Dependency, Path> loaded = new HashMap<>();
    private final Map<ImmutableSet<Dependency>, IsolatedClassLoader> loaders = new HashMap<>();
    private RelocationHandler relocationHandler = null;

    public DependencyManager(KumaPlugin plugin) {
        this.plugin = plugin;
        this.registry = new DependencyRegistry(plugin);
        this.cacheDirectory = setupCacheDirectory(plugin);
    }

    private static @NotNull Path setupCacheDirectory(@NotNull KumaPlugin plugin) {
        Path cacheDirectory = plugin.getBootstrap().getDirectory().resolve("libs");
        try {
            MoreFiles.createDirectoriesIfNotExists(cacheDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create libs directory", e);
        }
        return cacheDirectory;
    }

    private synchronized RelocationHandler getRelocationHandler() {
        if (this.relocationHandler == null) {
            this.relocationHandler = new RelocationHandler(this);
        }
        return this.relocationHandler;
    }

    public IsolatedClassLoader obtainClassLoaderWith(Set<Dependency> dependencies) {
        ImmutableSet<Dependency> set = ImmutableSet.copyOf(dependencies);

        for (Dependency dependency : dependencies) {
            if (!this.loaded.containsKey(dependency)) {
                throw new IllegalStateException("Dependency " + dependency + " is not loaded.");
            }
        }

        synchronized (this.loaders) {
            IsolatedClassLoader classLoader = this.loaders.get(set);
            if (classLoader != null) {
                return classLoader;
            }

            URL[] urls = set.stream()
                    .map(this.loaded::get)
                    .map(file -> {
                        try {
                            return file.toUri().toURL();
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toArray(URL[]::new);

            classLoader = new IsolatedClassLoader(urls);
            this.loaders.put(set, classLoader);
            return classLoader;
        }
    }

    public void loadStorageDependencies(Set<StorageType> storageTypes) {
        loadDependencies(this.registry.resolveStorageDependencies(storageTypes));
    }

    public void loadDependencies(@NotNull Set<Dependency> dependencies) {
        CountDownLatch latch = new CountDownLatch(dependencies.size());

        for (Dependency dependency : dependencies) {
            this.plugin.getBootstrap().getScheduler().async().execute(() -> {
                try {
                    loadDependency(dependency);
                } catch (Throwable e) {
                    this.plugin.getLogger().severe("Unable to load dependency " + dependency.name() + ".", e);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void loadDependency(Dependency dependency) throws Exception {
        if (this.loaded.containsKey(dependency)) {
            return;
        }

        Path file = remapDependency(dependency, downloadDependency(dependency));

        this.loaded.put(dependency, file);

        if (this.registry.shouldAutoLoad(dependency)) {
            this.plugin.getBootstrap().getClassPathAppender().addJarToClasspath(file);
        }
    }

    private Path downloadDependency(@NotNull Dependency dependency) throws DependencyDownloadException {
        Path file = this.cacheDirectory.resolve(dependency.getFileName(null));
        if (Files.exists(file)) {
            return file;
        }
        DependencyDownloadException lastError = null;
        for (DependencyRepository repo : DependencyRepository.values()) {
            try {
                repo.download(dependency, file);
                return file;
            } catch (DependencyDownloadException e) {
                lastError = e;
            }
        }

        throw Objects.requireNonNull(lastError);
    }

    private Path remapDependency(@NotNull Dependency dependency, Path normalFile) throws Exception {
        List<Relocation> rules = new ArrayList<>(dependency.getRelocations());
        this.registry.applyRelocationSettings(dependency, rules);
        if (rules.isEmpty()) {
            return normalFile;
        }
        Path remappedFile = this.cacheDirectory.resolve(dependency.getFileName(DependencyRegistry.isGsonRelocated() ? "remapped-legacy" : "remapped"));
        if (Files.exists(remappedFile)) {
            return remappedFile;
        }
        getRelocationHandler().remap(normalFile, remappedFile, rules);
        return remappedFile;
    }
}