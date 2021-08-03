package com.realxode.report.common.plugin.classpath;

import org.jetbrains.annotations.NotNull;
import xyz.zyran.hashbridge.common.plugin.bootstrap.HashBridgeBootstrap;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

@Deprecated
public class ReflectionClassPathAppender implements ClassPathAppender {
    private static final Method ADD_URL_METHOD;

    static {
        try {
            openUrlClassLoaderModule();
        } catch (Throwable e) {
            // ignore
        }

        try {
            ADD_URL_METHOD = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        try {
            ADD_URL_METHOD.setAccessible(true);
        } catch (Throwable e) {
            new RuntimeException("LuckPerms is unable to access the URLClassLoader#addURL method using reflection. \n" +
                    "You may be able to fix this problem by adding the following command-line argument " +
                    "directly after the 'java' command in your start script: \n'--add-opens java.base/java.lang=ALL-UNNAMED'", e).printStackTrace();
        }
    }

    private final URLClassLoader classLoader;

    public ReflectionClassPathAppender(@NotNull HashBridgeBootstrap bootstrap) throws IllegalStateException {
        ClassLoader classLoader = bootstrap.getClass().getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            this.classLoader = (URLClassLoader) classLoader;
        } else {
            throw new IllegalStateException("ClassLoader is not instance of URLClassLoader");
        }
    }

    public static void addUrl(URLClassLoader classLoader, URL url) throws ReflectiveOperationException {
        ADD_URL_METHOD.invoke(classLoader, url);
    }

    private static void openUrlClassLoaderModule() throws Exception {
        Class<?> moduleClass = Class.forName("java.lang.Module");
        Method getModuleMethod = Class.class.getMethod("getModule");
        Method addOpensMethod = moduleClass.getMethod("addOpens", String.class, moduleClass);

        Object urlClassLoaderModule = getModuleMethod.invoke(URLClassLoader.class);
        Object thisModule = getModuleMethod.invoke(ReflectionClassPathAppender.class);

        addOpensMethod.invoke(urlClassLoaderModule, URLClassLoader.class.getPackage().getName(), thisModule);
    }

    @Override
    public void addJarToClasspath(@NotNull Path file) {
        try {
            addUrl(this.classLoader, file.toUri().toURL());
        } catch (ReflectiveOperationException | MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
