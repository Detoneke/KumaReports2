package com.realxode.report.common.dependencies.relocation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Relocation {

    private final String pattern;
    private final String relocatedPattern;

    private Relocation(String pattern, String relocatedPattern) {
        this.pattern = pattern;
        this.relocatedPattern = relocatedPattern;
    }

    @Contract("_, _ -> new")
    public static @NotNull Relocation of(String id, @NotNull String pattern) {
        return new Relocation(pattern.replace("{}", "."), "xyz.zyran.thebridge.lib." + id);
    }

    public String getPattern() {
        return this.pattern;
    }

    public String getRelocatedPattern() {
        return this.relocatedPattern;
    }

}