package com.bazelbuild.java.classpath;

public class DummyFileEntry {
    final String path;
    final String content;

    public DummyFileEntry(String path, String content) {
        this.path = path;
        this.content = content;
    }
}