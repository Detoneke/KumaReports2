package com.realxode.report.normal;

import com.realxode.report.common.plugin.bootstrap.KumaBootstrap;
import com.realxode.report.loader.LoaderBootstrap;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@SuppressWarnings("unused")
public class KBootstrap implements LoaderBootstrap, KumaBootstrap {

    private final JavaPlugin loader;
    private final Path directory;

    public KBootstrap(@NotNull JavaPlugin loader) {
        this.loader = loader;
        directory = loader.getDataFolder().toPath();
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    public JavaPlugin getLoader() {
        return loader;
    }

    @Override
    public Path getDirectory() {
        return directory;
    }
}