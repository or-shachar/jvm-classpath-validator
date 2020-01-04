package com.bazelbuild.java.classpath;

public class ClasspathValidatorJarInput {
    final String label;
    final String jarPath;

    public ClasspathValidatorJarInput(String label, String jarPath){
        this.label = label;
        this.jarPath = jarPath;
    }
}
