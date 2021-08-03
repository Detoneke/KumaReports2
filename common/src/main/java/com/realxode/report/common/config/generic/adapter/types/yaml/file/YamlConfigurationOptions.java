package com.realxode.report.common.config.generic.adapter.types.yaml.file;

import com.realxode.report.common.config.generic.adapter.types.yaml.utils.Validate;

import java.util.Objects;

public class YamlConfigurationOptions extends FileConfigurationOptions {

    protected YamlConfigurationOptions(final YamlConfiguration configuration) {
        super(configuration);
    }

    @Override
    public YamlConfiguration configuration() {
        return (YamlConfiguration) super.configuration();
    }

    @Override
    public YamlConfigurationOptions copyDefaults(final boolean value) {
        super.copyDefaults(value);
        return this;
    }

    @Override
    public YamlConfigurationOptions pathSeparator(final char value) {
        super.pathSeparator(value);
        return this;
    }

    @Override
    public YamlConfigurationOptions header(final String value) {
        super.header(value);
        return this;
    }

    @Override
    public YamlConfigurationOptions copyHeader(final boolean value) {
        super.copyHeader(value);
        return this;
    }

    @Override
    public YamlConfigurationOptions indent(final int value) {
        Validate.isTrue(value >= 2, "Indent must be at least 2 characters");
        Validate.isTrue(value <= 9, "Indent cannot be greater than 9 characters");
        super.indent(value);
        return this;
    }

    @Override
    public int hashCode() {
        int indent = 2;
        return Objects.hash(super.hashCode(), indent);
    }
}
