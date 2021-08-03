package com.realxode.report.loader;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class JarClassLoader extends URLClassLoader {

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public JarClassLoader(ClassLoader parent, String resource) {
        super(new URL[]{extractJar(parent, resource)}, parent);
    }

    private static @NotNull URL extractJar(@NotNull ClassLoader classLoader, String resource) {
        URL jar = classLoader.getResource(resource);
        if (jar == null) {
            throw new LoadingException("Could not locate jar");
        }

        Path path;
        try {
            path = Files.createTempFile("kuma-reports", ".jar.temp");
        } catch (IOException e) {
            throw new LoadingException("Unable create a temporary file", e);
        }
        path.toFile().deleteOnExit();
        try (InputStream input = jar.openStream()) {
            Files.copy(input, path, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new LoadingException("Unable copy jar to temporary path", e);
        }
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new LoadingException("Unable to get URL from path", e);
        }
    }

    public LoaderBootstrap instantiatePlugin(String bootstrapClass, JavaPlugin loader) {
        Class<? extends LoaderBootstrap> plugin;
        try {
            plugin = loadClass(bootstrapClass).asSubclass(LoaderBootstrap.class);
        } catch (ClassNotFoundException e) {
            throw new LoadingException("Unable to load bootstrap class", e);
        }

        Constructor<? extends LoaderBootstrap> constructor;
        try {
            constructor = plugin.getConstructor(JavaPlugin.class);
        } catch (NoSuchMethodException e) {
            throw new LoadingException("Unable to get bootstrap constructor", e);
        }

        try {
            return constructor.newInstance(loader);
        } catch (ReflectiveOperationException e) {
            throw new LoadingException("Unable to create bootstrap instance", e);
        }
    }
}