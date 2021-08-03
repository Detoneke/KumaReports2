package com.realxode.report.common.config.generic.key;

import com.realxode.report.common.config.generic.adapter.ConfigurationAdapter;

public interface ConfigKey<T> {

    int ordinal();

    boolean reloadable();

    T get(ConfigurationAdapter adapter);

}
