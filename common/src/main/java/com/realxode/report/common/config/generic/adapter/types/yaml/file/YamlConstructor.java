package com.realxode.report.common.config.generic.adapter.types.yaml.file;

import com.realxode.report.common.config.generic.adapter.types.yaml.serialization.ConfigurationSerialization;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.LinkedHashMap;
import java.util.Map;

public final class YamlConstructor extends SafeConstructor {

    public YamlConstructor() {
        this.yamlConstructors.put(Tag.MAP, new ConstructCustomObject());
    }

    private final class ConstructCustomObject extends ConstructYamlMap {

        @Override
        public Object construct(final Node node) {
            if (node.isTwoStepsConstruction()) {
                throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
            }
            final Map<?, ?> raw = (Map<?, ?>) super.construct(node);
            if (!raw.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                return raw;
            }
            final Map<String, Object> typed = new LinkedHashMap<>(raw.size());
            for (final Map.Entry<?, ?> entry : raw.entrySet()) {
                typed.put(entry.getKey().toString(), entry.getValue());
            }
            try {
                return ConfigurationSerialization.deserializeObject(typed);
            } catch (final IllegalArgumentException ex) {
                throw new YAMLException("Could not deserialize object", ex);
            }
        }

        @Override
        public void construct2ndStep(final Node node, final Object object) {
            throw new YAMLException("Unexpected referential mapping structure. Node: " + node);
        }

    }

}
