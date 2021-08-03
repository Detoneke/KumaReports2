package com.realxode.report.common.config.generic.adapter.types.yaml.serialization;

import com.realxode.report.common.config.generic.adapter.types.yaml.utils.Validate;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationSerialization {

    public static final String SERIALIZED_TYPE_KEY = "==";
    private static final Map<String, Class<? extends ConfigurationSerializable>> aliases = new HashMap<String, Class<? extends ConfigurationSerializable>>();

    private final Class<? extends ConfigurationSerializable> clazz;

    protected ConfigurationSerialization(final Class<? extends ConfigurationSerializable> clazz) {
        this.clazz = clazz;
    }

    public static ConfigurationSerializable deserializeObject(final Map<String, ?> args, final Class<? extends ConfigurationSerializable> clazz) {
        return new ConfigurationSerialization(clazz).deserialize(args);
    }

    public static ConfigurationSerializable deserializeObject(final @NotNull Map<String, ?> args) {
        Class<? extends ConfigurationSerializable> clazz = null;

        if (args.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
            try {
                final String alias = (String) args.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY);

                if (alias == null) {
                    throw new IllegalArgumentException("Cannot have null alias");
                }
                clazz = ConfigurationSerialization.getClassByAlias(alias);
                if (clazz == null) {
                    throw new IllegalArgumentException("Specified class does not exist ('" + alias + "')");
                }
            } catch (final ClassCastException ex) {
                ex.fillInStackTrace();
                throw ex;
            }
        } else {
            throw new IllegalArgumentException("Args doesn't contain type key ('" + ConfigurationSerialization.SERIALIZED_TYPE_KEY + "')");
        }

        return new ConfigurationSerialization(clazz).deserialize(args);
    }

    public static void registerClass(final Class<? extends ConfigurationSerializable> clazz) {
        final DelegateDeserialization delegate = clazz.getAnnotation(DelegateDeserialization.class);
        if (delegate == null) {
            ConfigurationSerialization.registerClass(clazz, ConfigurationSerialization.getAlias(clazz));
            ConfigurationSerialization.registerClass(clazz, clazz.getName());
        }
    }

    public static void registerClass(final Class<? extends ConfigurationSerializable> clazz, final String alias) {
        ConfigurationSerialization.aliases.put(alias, clazz);
    }

    public static void unregisterClass(final String alias) {
        ConfigurationSerialization.aliases.remove(alias);
    }

    public static void unregisterClass(final Class<? extends ConfigurationSerializable> clazz) {
        ConfigurationSerialization.aliases.values().remove(clazz);
    }

    public static Class<? extends ConfigurationSerializable> getClassByAlias(final String alias) {
        return ConfigurationSerialization.aliases.get(alias);
    }

    public static String getAlias(final Class<? extends ConfigurationSerializable> clazz) {
        DelegateDeserialization delegate = clazz.getAnnotation(DelegateDeserialization.class);

        if (delegate != null) {
            return ConfigurationSerialization.getAlias(delegate.value());
        } else {
            final SerializableAs alias = clazz.getAnnotation(SerializableAs.class);
            if (alias != null) {
                return alias.value();
            }
        }

        return clazz.getName();
    }

    public ConfigurationSerializable deserialize(final Map<String, ?> args) {
        Validate.notNull(args, "Args must not be null");
        ConfigurationSerializable result = null;
        Method method;

        method = this.getMethod("deserialize");

        if (method != null) {
            result = this.deserializeViaMethod(method, args);
        }

        if (result == null) {
            method = this.getMethod("valueOf");

            if (method != null) {
                result = this.deserializeViaMethod(method, args);
            }
        }

        if (result == null) {
            final Constructor<? extends ConfigurationSerializable> constructor = this.getConstructor();

            if (constructor != null) {
                result = this.deserializeViaCtor(constructor, args);
            }
        }

        return result;
    }

    protected Method getMethod(final String name) {
        try {
            final Method method = this.clazz.getDeclaredMethod(name, Map.class);

            if (!ConfigurationSerializable.class.isAssignableFrom(method.getReturnType())) {
                return null;
            }
            if (!Modifier.isStatic(method.getModifiers())) {
                return null;
            }

            return method;
        } catch (final NoSuchMethodException | SecurityException ex) {
            return null;
        }
    }

    protected Constructor<? extends ConfigurationSerializable> getConstructor() {
        try {
            return this.clazz.getConstructor(Map.class);
        } catch (final NoSuchMethodException | SecurityException ex) {
            return null;
        }
    }

    protected ConfigurationSerializable deserializeViaMethod(final Method method, final Map<String, ?> args) {
        try {
            final ConfigurationSerializable result = (ConfigurationSerializable) method.invoke(null, args);

            if (result == null) {
                Logger.getLogger(ConfigurationSerialization.class.getName()).log(Level.SEVERE, "Could not call method '" + method.toString() + "' of " + this.clazz + " for deserialization: method returned null");
            } else {
                return result;
            }
        } catch (final Throwable ex) {
            Logger.getLogger(ConfigurationSerialization.class.getName()).log(
                    Level.SEVERE,
                    "Could not call method '" + method.toString() + "' of " + this.clazz + " for deserialization",
                    ex instanceof InvocationTargetException ? ex.getCause() : ex);
        }

        return null;
    }

    protected ConfigurationSerializable deserializeViaCtor(final Constructor<? extends ConfigurationSerializable> ctor, final Map<String, ?> args) {
        try {
            return ctor.newInstance(args);
        } catch (final Throwable ex) {
            Logger.getLogger(ConfigurationSerialization.class.getName()).log(
                    Level.SEVERE,
                    "Could not call constructor '" + ctor.toString() + "' of " + this.clazz + " for deserialization",
                    ex instanceof InvocationTargetException ? ex.getCause() : ex);
        }

        return null;
    }

}