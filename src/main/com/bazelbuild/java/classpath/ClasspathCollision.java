package com.bazelbuild.java.classpath;

import java.util.List;

public class ClasspathCollision {
    final String path;
    final List<String> targets;

    public ClasspathCollision(String path, List<String> targets) {
        this.path = path;
        this.targets = targets;
    }
}
