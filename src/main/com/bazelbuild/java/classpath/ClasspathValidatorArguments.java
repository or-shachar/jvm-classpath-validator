package com.bazelbuild.java.classpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClasspathValidatorArguments {
    private List<String> ignorePrefix = new ArrayList<>();
    private List<String> ignoreSuffix = new ArrayList<>();
    private List<String> includePrefix = new ArrayList<>();
    private List<String> includeSuffix = new ArrayList<>();
    private String jarTargets;

    final private String JarTargets = "--jar-targets";
    final private String IgnorePrefix = "--ignore-prefix";
    final private String IgnoreSuffix = "--ignore-suffix";
    final private String IncludePrefix = "--include-prefix";
    final private String IncludeSuffix = "--include-suffix";


    public ClasspathValidatorArguments(String[] args) {
        List<String> arguments = Arrays.asList(args);

        List<String> jarTargetsArgs = extractParameters(JarTargets, arguments);

        if (jarTargetsArgs.size() != 1) {
            throw new IllegalArgumentException(String.format("'%s' must be specified exactly once", JarTargets));
        }
        jarTargets = jarTargetsArgs.get(0);

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

    public String getJarTargets() {
        return jarTargets;
    }
}
