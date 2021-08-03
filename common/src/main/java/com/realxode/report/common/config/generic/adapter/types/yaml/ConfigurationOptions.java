package com.realxode.report.common.config.generic.adapter.types.yaml;

import java.util.Objects;

public class ConfigurationOptions {

    private final Configuration configuration;

    private char pathSeparator = '.';

    private boolean copyDefaults = true;

    private int indent = 2;

    protected ConfigurationOptions(final Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration configuration() {
        return this.configuration;
    }

    public char pathSeparator() {
        return this.pathSeparator;
    }

    public ConfigurationOptions pathSeparator(final char value) {
        this.pathSeparator = value;
        return this;
    }

    public boolean copyDefaults() {
        return this.copyDefaults;
    }

    public ConfigurationOptions copyDefaults(final boolean value) {
        this.copyDefaults = value;
        return this;
    }

    public int indent() {
        return this.indent;
    }

    public ConfigurationOptions indent(int value) {
        this.indent = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigurationOptions)) return false;
        ConfigurationOptions that = (ConfigurationOptions) o;
        return pathSeparator == that.pathSeparator &&
                copyDefaults == that.copyDefaults &&
                Objects.equals(configuration, that.configuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuration, pathSeparator, copyDefaults);
    }
}
