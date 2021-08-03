package com.realxode.report.common.config.generic.adapter.types.yaml.file;

import com.realxode.report.common.config.generic.adapter.types.yaml.Configuration;
import com.realxode.report.common.config.generic.adapter.types.yaml.ConfigurationSection;
import com.realxode.report.common.config.generic.adapter.types.yaml.comments.YamlCommentMapper;
import com.realxode.report.common.config.generic.adapter.types.yaml.exceptions.InvalidConfigurationException;
import com.realxode.report.common.config.generic.adapter.types.yaml.utils.Validate;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YamlConfiguration extends FileConfiguration {

    protected static final String BLANK_CONFIG = "{}\n";

    private final DumperOptions yamlOptions = new DumperOptions();
    private final Representer yamlRepresenter = new YamlRepresenter();
    private final Yaml yaml = new Yaml(new YamlConstructor(), this.yamlRepresenter, this.yamlOptions);

    public static @NotNull YamlConfiguration loadConfiguration(final File file) {
        Validate.notNull(file, "File cannot be null");
        return YamlConfiguration.load(config -> config.load(file));
    }

    public static @NotNull YamlConfiguration loadConfiguration(final InputStream stream) {
        Validate.notNull(stream, "Stream cannot be null");
        return YamlConfiguration.load(config -> config.load(stream));
    }

    public static @NotNull YamlConfiguration loadConfiguration(final Reader reader) {
        Validate.notNull(reader, "Reader cannot be null");
        return YamlConfiguration.load(config -> config.load(reader));
    }

    @Override
    public String saveToString() throws IOException {
        return this.buildHeader() + this.dump();
    }

    protected String dump() {
        this.yamlOptions.setIndent(this.options().indent());
        this.yamlOptions.setIndicatorIndent(this.options().indent());
        this.yamlOptions.setAllowUnicode(this.options().isUnicode());
        this.yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        String dump = this.yaml.dump(this.getValues(false));
        if (dump.equals(YamlConfiguration.BLANK_CONFIG)) {
            dump = "";
        }
        return dump;
    }

    @Override
    public void loadFromString(final String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, "Contents cannot be null");

        final Map<?, ?> input;
        try {
            input = this.yaml.load(contents);
        } catch (final YAMLException e) {
            throw new InvalidConfigurationException(e);
        } catch (final ClassCastException e) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }

        final String header = YamlConfiguration.parseHeader(contents);
        if (!header.isEmpty()) {
            this.options().header(header);
        }

        if (input != null) {
            this.convertMapsToSections(input, this);
        }
    }

    @Override
    public String buildHeader() {
        final String header = this.options().header();
        final boolean copyHeader = this.options().copyHeader();

        if (!copyHeader || header == null) {
            return "";
        }

        final Configuration def = this.getDefaults();

        if (def instanceof FileConfiguration) {
            final FileConfiguration filedefaults = (FileConfiguration) def;
            final String defaultsHeader = filedefaults.buildHeader();

            if (defaultsHeader != null && defaultsHeader.length() > 0) {
                return defaultsHeader;
            }
        }

        final StringBuilder builder = new StringBuilder();
        final String[] lines = header.split("\r?\n", -1);
        boolean startedHeader = false;

        for (int i = lines.length - 1; i >= 0; i--) {
            builder.insert(0, "\n");

            if (startedHeader || lines[i].length() != 0) {
                builder.insert(0, lines[i]);
                startedHeader = true;
            }
        }

        return builder.toString();
    }

    @Override
    public YamlConfigurationOptions options() {
        if (this.options == null) {
            this.options = new YamlConfigurationOptions(this);
        }
        return (YamlConfigurationOptions) this.options;
    }

    protected void convertMapsToSections(final @NotNull Map<?, ?> input, final ConfigurationSection section) {
        for (final Map.Entry<?, ?> entry : input.entrySet()) {
            final String key = entry.getKey().toString();
            final Object value = entry.getValue();

            if (value instanceof Map) {
                this.convertMapsToSections((Map<?, ?>) value, section.createSection(key));
            } else {
                section.set(key, value);
            }
        }
    }

    protected static @NotNull String parseHeader(final @NotNull String input) {
        final String[] lines = input.split("\r?\n", -1);
        final StringBuilder result = new StringBuilder();
        boolean readingHeader = true;
        boolean foundHeader = false;

        String commentPrefixTrimmed = YamlCommentMapper.COMMENT_PREFIX.trim();

        for (int lineindex = 0; lineindex < lines.length && readingHeader; lineindex++) {
            final String line = lines[lineindex];

            if (line.startsWith(commentPrefixTrimmed)) {
                if (lineindex > 0) {
                    result.append('\n');
                }

                if (line.length() > commentPrefixTrimmed.length()) {
                    result.append(line.trim());
                }

                foundHeader = true;
            } else if (foundHeader && line.isEmpty()) {
                result.append('\n');
            } else if (foundHeader) {
                readingHeader = false;
            }
        }

        return result.toString();
    }

    private static @NotNull YamlConfiguration load(final @NotNull YamlConfigurationLoader loader) {
        final YamlConfiguration config = new YamlConfiguration();

        try {
            loader.load(config);
        } catch (final IOException | InvalidConfigurationException ex) {
            Logger.getLogger(YamlConfiguration.class.getName()).log(Level.SEVERE, "Cannot load configuration", ex);
        }

        return config;
    }

    private interface YamlConfigurationLoader {

        void load(YamlConfiguration config) throws IOException, InvalidConfigurationException;

    }

}
