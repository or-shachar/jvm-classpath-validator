package com.bazelbuild.java.classpath;

import javafx.util.Pair;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class ClassPathValidator {
    private static List<String> prefixIgnore = Arrays.asList("META-INF");
    private static List<String> suffixIgnore = Arrays.asList("LICENSE", "pom.xml", "BUILD.bazel", "module-info.class", "LICENSE.txt", "NOTICE", "mozilla/public-suffix-list.txt", "rootdoc.txt");

    public static List<ClasspathCollision> collisionsIn(List<ClasspathValidatorJarInput> jars) throws IOException {
        Map<String, Map<String, String>> targetsToJarEntriesToDigests = extractEntries(jars);
        List<ClasspathCollision> classpathCollisions = computeCollisions(targetsToJarEntriesToDigests);
        return classpathCollisions;
    }

    private static List<ClasspathCollision> computeCollisions(Map<String, Map<String, String>> targetsToJarEntriesToDigests) {
        List<Pair<String, String>> pairs = allTargetsPairsIn(new ArrayList<>(targetsToJarEntriesToDigests.keySet()));
        return pairs.stream()
                .map(p -> collisionsBetween(targetsToJarEntriesToDigests, p))
                .filter(c -> !c.differentEntries.isEmpty())
                .collect(Collectors.toList());
    }

    private static ClasspathCollision collisionsBetween(Map<String, Map<String, String>> targetsToJarEntriesToDigests,
                                                        Pair<String, String> targetsPair) {
        String target1 = targetsPair.getKey();
        String target2 = targetsPair.getValue();
        List<String> collisions = findCollisionsIn(targetsToJarEntriesToDigests.get(target1), targetsToJarEntriesToDigests.get(target2));
        return new ClasspathCollision(target1, target2, collisions);
    }

    private static List<String> findCollisionsIn(Map<String, String> jar1EntriesToDigest, Map<String, String> jar2EntriesToDigest) {
        return jar1EntriesToDigest.keySet().stream().filter(f ->
                jar2EntriesToDigest.containsKey(f) && !jar1EntriesToDigest.get(f).equals(jar2EntriesToDigest.get(f))
        ).collect(Collectors.toList());
    }

    private static List<Pair<String, String>> allTargetsPairsIn(List<String> originalSet) {
        if (originalSet.isEmpty())
            return Collections.emptyList();
        String head = originalSet.get(0);
        List<String> tail = originalSet.subList(1, originalSet.size());

        List<Pair<String, String>> currentPairs = new ArrayList<>(tail.stream().map(s -> new Pair<>(head, s)).collect(Collectors.toList()));

        currentPairs.addAll(allTargetsPairsIn(tail));
        return currentPairs;
    }


    private static Map<String, Map<String, String>> extractEntries(List<ClasspathValidatorJarInput> jars) throws IOException {
        Map<String, Map<String, String>> m = new HashMap<>();
        for (ClasspathValidatorJarInput j : jars) {
            m.put(j.label, ClasspathEntries.getEntries(j.jarPath).stream()
                    .filter(g -> !ignored(g.path))
                    .collect(Collectors.toMap(MiniJarEntry::getPath, MiniJarEntry::getDigest)));
        }
        return Collections.unmodifiableMap(m);
    }

    private static boolean ignored(String path) {
        return prefixIgnore.stream().anyMatch(path::startsWith) ||
                suffixIgnore.stream().anyMatch(path::endsWith);
    }
}
