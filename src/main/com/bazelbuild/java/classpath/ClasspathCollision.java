package com.bazelbuild.java.classpath;

import java.util.List;

public class ClasspathCollision {
    final String target1;
    final String target2;
    final String targets;
    final List<String> differentEntries;

    public ClasspathCollision(String target1, String target2, List<String> differentEntries){
        if (target1.compareTo(target2) < 0) {
            this.target1 = target1;
            this.target2 = target2;
        } else {
            this.target2 = target1;
            this.target1 = target2;
        }
        this.differentEntries = differentEntries;
        this.targets = this.target1 + " <> " + this.target2;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(targets);
        b.append("\n------\n");
        for (String c: differentEntries)
            b.append("\t "+c+"\n");
        return b.toString();
    }

}
