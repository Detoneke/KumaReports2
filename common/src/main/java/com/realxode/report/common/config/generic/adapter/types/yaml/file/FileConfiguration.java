package com.realxode.report.common.config.generic.adapter.types.yaml.file;

import com.realxode.report.common.config.generic.adapter.types.yaml.Configuration;
import com.realxode.report.common.config.generic.adapter.types.yaml.MemoryConfiguration;
import com.realxode.report.common.config.generic.adapter.types.yaml.exceptions.InvalidConfigurationException;
import com.realxode.report.common.config.generic.adapter.types.yaml.utils.Validate;

import java.io.*;

public abstract class FileConfiguration extends MemoryConfiguration {

    public FileConfiguration() {
        super();
    }

    public FileConfiguration(final Configuration defaults) {
        super(defaults);
    }

    public void save(final File file) throws IOException {
        Validate.notNull(file, "File cannot be null");
        this.write(file, this.saveToString());
    }

    public void save(final String file) throws IOException {
        Validate.notNull(file, "File cannot be null");
        this.save(new File(file));
    }

    public abstract String saveToString() throws IOException;


    public void load(final String file) throws IOException, InvalidConfigurationException {
        Validate.notNull(file, "File cannot be null");
        this.load(new File(file));
    }

    public void load(final File file) throws FileNotFoundException, IOException, InvalidConfigurationException {
        Validate.notNull(file, "File cannot be null");
        load(new FileInputStream(file));
    }


    public void load(final InputStream stream) throws IOException, InvalidConfigurationException {
        Validate.notNull(stream, "Stream cannot be null");
        load(new InputStreamReader(stream, this.options().charset()));
    }

    public void load(final Reader reader) throws IOException, InvalidConfigurationException {
        try (final BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader)) {
            final StringBuilder builder = new StringBuilder();
            String line;

            while ((line = input.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }

            this.loadFromString(builder.toString());
        }
    }

    public abstract void loadFromString(String contents) throws InvalidConfigurationException;

    @Override
    public FileConfigurationOptions options() {
        if (this.options == null) {
            this.options = new FileConfigurationOptions(this);
        }
        return (FileConfigurationOptions) this.options;
    }

    protected void write(final File file, final String data) throws IOException {
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        try (final Writer writer = new OutputStreamWriter(new FileOutputStream(file), this.options().charset())) {
            writer.write(data);
        }
    }

    public String buildHeader() {
        return "";
    }

}
