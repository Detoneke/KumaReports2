package com.realxode.report.common.config.generic.adapter.types.yaml.file;

import com.realxode.report.common.config.generic.adapter.types.yaml.MemoryConfiguration;
import com.realxode.report.common.config.generic.adapter.types.yaml.MemoryConfigurationOptions;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class FileConfigurationOptions extends MemoryConfigurationOptions {

    private Charset charset = StandardCharsets.UTF_8;

    private String header = null;

    private boolean copyHeader = true;

    protected FileConfigurationOptions(final MemoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public FileConfiguration configuration() {
        return (FileConfiguration) super.configuration();
    }

    @Override
    public FileConfigurationOptions pathSeparator(final char value) {
        super.pathSeparator(value);
        return this;
    }

    @Override
    public FileConfigurationOptions copyDefaults(final boolean value) {
        super.copyDefaults(value);
        return this;
    }

    public Charset charset() {
        return this.charset;
    }

    public FileConfigurationOptions charset(final Charset charset) {
        this.charset = charset;
        return this;
    }

    public boolean isUnicode() {
        return this.charset.name().startsWith("UTF");
    }

    public String header() {
        return this.header;
    }

    public FileConfigurationOptions header(final String value) {
        this.header = value;
        return this;
    }

    public boolean copyHeader() {
        return this.copyHeader;
    }

    public FileConfigurationOptions copyHeader(final boolean value) {
        this.copyHeader = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileConfigurationOptions)) return false;
        if (!super.equals(o)) return false;
        FileConfigurationOptions that = (FileConfigurationOptions) o;
        return copyHeader == that.copyHeader &&
                Objects.equals(charset, that.charset) &&
                Objects.equals(header, that.header);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), charset, header, copyHeader);
    }
}
