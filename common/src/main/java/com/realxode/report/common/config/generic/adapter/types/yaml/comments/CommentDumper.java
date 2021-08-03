package com.realxode.report.common.config.generic.adapter.types.yaml.comments;

import com.realxode.report.common.config.generic.adapter.types.yaml.file.YamlConfigurationOptions;

import java.io.IOException;
import java.io.Reader;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommentDumper extends YamlCommentReader {

    private final YamlCommentMapper yamlCommentMapper;
    private StringBuilder builder;

    public CommentDumper(final YamlConfigurationOptions options, final YamlCommentMapper yamlCommentMapper, final Reader reader) {
        super(options, reader);
        this.yamlCommentMapper = yamlCommentMapper;
    }

    public String dump() throws IOException {
        if (this.yamlCommentMapper == null) {
            return this.reader.lines().collect(Collectors.joining("\n"));
        }

        this.builder = new StringBuilder();

        while (this.nextLine()) {
            if (!this.isComment()) {
                final String path = this.track().getPath();
                final KeyTree.Node node = this.getNode(path);
                this.append(node, KeyTree.Node::getComment);
                this.builder.append(this.currentLine);
                this.append(node, KeyTree.Node::getSideComment);
                this.builder.append('\n');
            }
        }
        this.append(this.getNode(null), KeyTree.Node::getComment);
        this.reader.close();
        return this.builder.toString();
    }

    @Override
    protected KeyTree.Node getNode(final String path) {
        return this.yamlCommentMapper.getNode(path);
    }

    private void append(final KeyTree.Node node, final Function<KeyTree.Node, String> getter) {
        if (node != null) {
            final String s = getter.apply(node);
            if (s != null) {
                this.builder.append(s);
            }
        }
    }

}
