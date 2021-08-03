package com.realxode.report.common.plugin.bootstrap;

import com.realxode.report.common.plugin.classpath.ClassPathAppender;
import com.realxode.report.common.plugin.scheduler.SchedulerAdapter;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public interface KumaBootstrap {

    Path getDirectory();

    SchedulerAdapter getScheduler();

    Server getServer();

    JavaPlugin getLoader();

    ClassPathAppender getClassPathAppender();
}