package com.realxode.report.common.plugin;

import com.realxode.report.common.plugin.bootstrap.KumaBootstrap;
import com.realxode.report.common.plugin.logging.PluginLogger;

public interface KumaPlugin {

    KumaBootstrap getBootstrap();

    PluginLogger getLogger();

}