package com.realxode.report.common.config.generic.adapter.types.yaml;

import com.realxode.report.common.config.generic.adapter.types.yaml.comments.*;
import com.realxode.report.common.config.generic.adapter.types.yaml.exceptions.InvalidConfigurationException;
import com.realxode.report.common.config.generic.adapter.types.yaml.file.YamlConfiguration;
import com.realxode.report.common.config.generic.adapter.types.yaml.utils.Validate;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YamlConfigurationAdapter extends YamlConfiguration implements Commentable {

    private File configFile;
    private YamlCommentMapper yamlCommentMapper;
    private boolean useComments = true;

    public YamlConfigurationAdapter() {
    }

    public YamlConfigurationAdapter(final String path) throws IllegalArgumentException {
        this.setConfigurationFile(path);
    }

    public YamlConfigurationAdapter(final File file) throws IllegalArgumentException {
        this.setConfigurationFile(file);
    }

    public YamlConfigurationAdapter(final URI uri) throws IllegalArgumentException {
        this.setConfigurationFile(uri);
    }

    public YamlConfigurationAdapter(final @NotNull URL url) throws IllegalArgumentException, URISyntaxException {
        this(url.toURI());
    }

    public void save() throws IOException {
        Validate.notNull(this.configFile, "This configuration file is null!");
        this.save(this.configFile);
    }

    public String saveToString() throws IOException {
        if (this.useComments) {
            return new CommentDumper(this.options(), this.parseComments(), new StringReader(super.dump())).dump();
        }
        return super.saveToString();
    }

    private YamlCommentMapper parseComments() throws IOException {
        if (this.yamlCommentMapper != null) {
            return this.yamlCommentMapper;
        }
        try {
            return parseComments(fileToString());
        } catch (InvalidConfigurationException e) {
            throw new IOException(e);
        }
    }

    private YamlCommentMapper parseComments(final String contents) throws InvalidConfigurationException {
        try {
            if (contents != null) {
                this.yamlCommentMapper = new YamlCommentParser(options(), new StringReader(contents));
                ((YamlCommentParser) this.yamlCommentMapper).parse();
            } else {
                this.yamlCommentMapper = new YamlCommentMapper(options());
            }
            return this.yamlCommentMapper;
        } catch (IOException e) {
            throw new InvalidConfigurationException(e);
        }
    }

    @Override
    public void setComment(final String path, final String comment, final CommentType type) {
        if (this.yamlCommentMapper == null) {
            this.useComments = true;
            this.yamlCommentMapper = new YamlCommentMapper(this.options());
        }
        this.yamlCommentMapper.setComment(path, comment, type);
    }

    @Override
    public String getComment(final String path, final CommentType type) {
        return this.yamlCommentMapper != null ? this.yamlCommentMapper.getComment(path, type) : null;
    }


    public void load() throws InvalidConfigurationException, IOException {
        Validate.notNull(this.configFile, "This configuration file is null!");
        this.load(this.configFile);
    }

    public void loadWithComments() throws InvalidConfigurationException, IOException {
        this.useComments = true;
        this.load();
    }

    @Override
    public void loadFromString(final String contents) throws InvalidConfigurationException {
        super.loadFromString(contents);
        if (this.useComments) {
            this.parseComments(contents);
        }
    }

    public void createOrLoadWithComments() throws IOException, InvalidConfigurationException {
        this.createNewFile(false);
        this.loadWithComments();
    }

    public boolean exists() {
        return this.configFile != null && this.configFile.exists();
    }

    public void createNewFile(final boolean overwrite) throws IOException {
        Validate.notNull(this.configFile, "This configuration file is null!");
        if (overwrite || !this.configFile.exists()) {
            try {
                final File parents = this.configFile.getParentFile();
                if (parents != null) {
                    parents.mkdirs();
                }
                this.configFile.createNewFile();
            } catch (final SecurityException e) {
                throw new IOException(e.getMessage(), e.getCause());
            }
        }
    }

    public void deleteFile() throws IOException {
        Validate.notNull(this.configFile, "This configuration file is null!");
        if (!this.configFile.delete()) {
            throw new IOException("Failed to delete " + this.configFile);
        }
    }

    public long getSize() {
        return this.configFile.length();
    }

    public String getFilePath() {
        Validate.notNull(this.configFile, "This configuration file is null!");
        return this.configFile.getAbsolutePath();
    }

    public File getConfigurationFile() {
        return this.configFile;
    }

    public void setConfigurationFile(final String path) throws IllegalArgumentException {
        Validate.notNull(path, "Path cannot be null.");
        setConfigFile(new File(path));
    }

    public void setConfigurationFile(final URI uri) throws IllegalArgumentException {
        Validate.notNull(uri, "URI cannot be null.");
        setConfigFile(new File(uri));
    }

    public void setConfigurationFile(final File file) throws IllegalArgumentException {
        Validate.notNull(file, "File cannot be null.");
        setConfigFile(file);
    }

    private void setConfigFile(final File file) throws IllegalArgumentException {
        this.configFile = file;
        if (this.configFile.isDirectory()) {
            String name = configFile.getName();
            this.configFile = null;
            throw new IllegalArgumentException(name + " is a directory!");
        }
    }

    public File copyTo(final String path) throws IllegalArgumentException, IOException {
        Validate.notNull(path, "Path cannot be null.");
        final File copy = new File(path);
        this.copyTo(copy);
        return copy;
    }

    public void copyTo(final File file) throws IllegalArgumentException, IOException {
        Validate.notNull(this.configFile, "This configuration file is null!");
        if (!this.configFile.exists()) {
            throw new FileNotFoundException(this.configFile.getName() + " is not found in " + this.configFile.getAbsolutePath());
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException(file.getAbsolutePath() + " is a directory!");
        }
        try (final OutputStream fos = Files.newOutputStream(file.toPath())) {
            Files.copy(this.configFile.toPath(), fos);
        } catch (final Exception exception) {
            exception.printStackTrace();
        }
    }

    public String fileToString() throws IOException {
        if (!exists()) {
            return null;
        }
        return new String(Files.readAllBytes(this.configFile.toPath()));
    }


    @Override
    public String toString() {
        try {
            return this.saveToString();
        } catch (final IOException e) {
            return e.getMessage();
        }
    }

    public static @NotNull YamlConfigurationAdapter loadConfiguration(final File file, boolean withComments) {
        Validate.notNull(file, "File cannot be null");
        return load(config -> {
            config.setConfigurationFile(file);
            config.load();
        }, withComments);
    }

    public static @NotNull YamlConfigurationAdapter loadConfiguration(final File file) {
        return loadConfiguration(file, false);
    }

    public static @NotNull YamlConfigurationAdapter loadConfiguration(final InputStream stream, boolean withComments) {
        Validate.notNull(stream, "Stream cannot be null");
        return load(config -> config.load(stream), withComments);
    }

    public static @NotNull YamlConfigurationAdapter loadConfiguration(final InputStream stream) {
        return loadConfiguration(stream, true);
    }

    public static @NotNull YamlConfigurationAdapter loadConfiguration(final File file, final InputStream stream) throws IOException {
        YamlConfigurationAdapter config = loadConfiguration(stream, true);
        config.setConfigFile(file);
        config.save(file);
        return config;
    }

    public static @NotNull YamlConfigurationAdapter loadConfiguration(final Reader reader, boolean withComments) {
        Validate.notNull(reader, "Reader cannot be null");
        return load(config -> config.load(reader), withComments);
    }

    public static @NotNull YamlConfigurationAdapter loadConfiguration(final Reader reader) {
        return loadConfiguration(reader, false);
    }

    private static @NotNull YamlConfigurationAdapter load(final @NotNull YamlFileLoader loader, boolean withComments) {
        final YamlConfigurationAdapter config = new YamlConfigurationAdapter();

        try {
            config.useComments = withComments;
            loader.load(config);
        } catch (final IOException | InvalidConfigurationException ex) {
            Logger.getLogger(YamlConfigurationAdapter.class.getName()).log(Level.SEVERE, "Cannot load configuration", ex);
        }

        return config;
    }

    private interface YamlFileLoader {

        void load(YamlConfigurationAdapter config) throws IOException, InvalidConfigurationException;

    }
}
