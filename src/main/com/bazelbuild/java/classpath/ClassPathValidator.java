package com.bazelbuild.java.classpath;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassPathValidator {

    public static List<ClasspathCollision> collisionsIn(List<ClasspathValidatorJarInput> jars) throws IOException, NoSuchAlgorithmException {
        Map<String, List<InnerJarEntry>> agg = new HashMap<>();
        aggregateData(jars, agg);
        return computeCollisions(agg);
    }

    private static List<ClasspathCollision> computeCollisions(Map<String, List<InnerJarEntry>> agg) {
        return agg.entrySet().stream()
                .filter(e-> e.getValue().size()>1)
                .filter(e-> differentContent(e.getValue()))
                .map(ClassPathValidator::toCollision)
                .collect(Collectors.toList());
    }

    private static ClasspathCollision toCollision(Map.Entry<String, List<InnerJarEntry>> entriesWithCollision) {
        String path = entriesWithCollision.getKey();
        return new ClasspathCollision(path,
                entriesWithCollision.getValue().stream()
                .map(e->e.sourceLabel)
                .collect(Collectors.toList()));
    }

    private static boolean differentContent(List<InnerJarEntry> entriesWithSamePath) {
        return entriesWithSamePath.stream()
                .map(e->e.digest)
                .distinct()
                .count() > 1;
    }

    private static void aggregateData(List<ClasspathValidatorJarInput> jars, Map<String, List<InnerJarEntry>> agg) throws IOException, NoSuchAlgorithmException {
        for (ClasspathValidatorJarInput jar : jars) {
            addToMap(agg, jar.label, ClasspathEntries.getEntries(Paths.get(jar.jarPath)));
        }
    }

    private static void addToMap(Map<String, List<InnerJarEntry>> agg, String label, List<MiniJarEntry> entries) {
        List<InnerJarEntry> current;
        for (MiniJarEntry entry : entries) {
            current = agg.getOrDefault(entry.path, new ArrayList<>());
            current.add(new InnerJarEntry(label,entry.digest));
            agg.put(entry.path, current);
        }
    }

    private static class InnerJarEntry {
        final String sourceLabel;
        final String digest;

        private InnerJarEntry(String sourceLabel, String digest) {
            this.sourceLabel = sourceLabel;
            this.digest = digest;
        }
    }
}
