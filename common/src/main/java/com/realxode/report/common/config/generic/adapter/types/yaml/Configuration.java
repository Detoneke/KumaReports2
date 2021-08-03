package com.realxode.report.common.config.generic.adapter.types.yaml;

import java.util.Map;

public interface Configuration extends ConfigurationSection {

    @Override
    void addDefault(String path, Object value);

    void addDefaults(Map<String, Object> defaults);

    void addDefaults(Configuration defaults);

    Configuration getDefaults();

    void setDefaults(Configuration defaults);

    ConfigurationOptions options();

}
