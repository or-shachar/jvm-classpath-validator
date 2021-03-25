package com.bazelbuild.java.classpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClasspathValidatorArguments {
    private List<String> ignorePrefix = new ArrayList<>();
    private List<String> ignoreSuffix = new ArrayList<>();
    private List<String> includePrefix = new ArrayList<>();
    private List<String> includeSuffix = new ArrayList<>();
    private List<String> jarTargets = new ArrayList<>();

    final private String JarTargets = "--jar-targets";
    final private String IgnorePrefix = "--ignore-prefix";
    final private String IgnoreSuffix = "--ignore-suffix";
    final private String IncludePrefix = "--include-prefix";
    final private String IncludeSuffix = "--include-suffix";

    final private List<String> Arguments = Arrays.asList(
        JarTargets, IgnorePrefix, IgnoreSuffix, IncludePrefix, IncludeSuffix
    );

    public ClasspathValidatorArguments(String[] args) {
        List<String> arguments = Arrays.asList(args);

        jarTargets = extractParameters(JarTargets, arguments);
        ignorePrefix = extractParameters(IgnorePrefix, arguments);
        ignoreSuffix = extractParameters(IgnoreSuffix, arguments);
        includePrefix = extractParameters(IncludePrefix, arguments);
        includeSuffix = extractParameters(IncludeSuffix, arguments);
    }

    private List<String> extractParameters(String parameterName, List<String> arguments) {
        List<String> collectedValues = new ArrayList<>();
        String prefix = String.format("%s=", parameterName);

        for (String arg : arguments) {
            if (arg.startsWith(prefix)) {
                String parsedValue = arg.substring(prefix.length());
                if (!parsedValue.isEmpty()) {
                    collectedValues.add(parsedValue);
                }
            }
        }

        return collectedValues;
    }

    public List<String> getIgnorePrefix() {
        return ignorePrefix;
    }

    public List<String> getIgnoreSuffix() {
        return ignoreSuffix;
    }

    public List<String> getIncludePrefix() {
        return includePrefix;
    }

    public List<String> getIncludeSuffix() {
        return includeSuffix;
    }

    public List<String> getJarTargets() {
        return jarTargets;
    }
}
