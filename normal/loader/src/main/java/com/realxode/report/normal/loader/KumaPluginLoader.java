package com.realxode.report.normal.loader;

import com.realxode.report.loader.JarClassLoader;
import com.realxode.report.loader.LoaderBootstrap;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public class KumaPluginLoader extends JavaPlugin {

    private final LoaderBootstrap bootstrap;

    public KumaPluginLoader() {
        JarClassLoader loader = new JarClassLoader(getClass().getClassLoader(), "kuma.report");
        bootstrap = loader.instantiatePlugin("com.realxode.report.normal.KBootstrap", this);
    }

    @Override
    public void onLoad() {
        bootstrap.onLoad();
    }

    @Override
    public void onEnable() {
        bootstrap.onEnable();
    }

    @Override
    public void onDisable() {
        bootstrap.onDisable();
    }
}
