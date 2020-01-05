package com.bazelbuild.java.classpath;

import java.nio.file.Path;

public class ClasspathValidatorJarInput {
    final String label;
    final Path jarPath;

    public ClasspathValidatorJarInput(String label, Path jarPath){
        this.label = label;
        this.jarPath = jarPath;
    }
}
