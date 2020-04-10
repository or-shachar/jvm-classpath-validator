package com.bazelbuild.java.classpath;

public class MiniJarEntry {
    final String path;
    final String digest;

    public MiniJarEntry(String path, String digest) {
        this.path = path;
        this.digest = digest;
    }

    public String getPath(){
        return path;
    }

    public String getDigest(){
        return digest;
    }

    @Override
    public String toString() {
        return String.format("MiniJarEntry(path=%s, digest=%s", this.path, this.digest);
    }
}
