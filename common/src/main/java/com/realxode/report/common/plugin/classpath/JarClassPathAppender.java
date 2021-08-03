package com.realxode.report.common.plugin.classpath;

import xyz.zyran.hashbridge.common.loader.JarClassLoader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

public class JarClassPathAppender implements ClassPathAppender {
    private final JarClassLoader classLoader;

    public JarClassPathAppender(ClassLoader classLoader) {
        if (!(classLoader instanceof JarClassLoader)) {
            throw new IllegalArgumentException("Loader is not a JarInJarClassLoader: " + classLoader.getClass().getName());
        }
        this.classLoader = (JarClassLoader) classLoader;
    }

    @Override
    public void addJarToClasspath(Path file) {
        try {
            this.classLoader.addJarToClasspath(file.toUri().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        this.classLoader.deleteJarResource();
        try {
            this.classLoader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
