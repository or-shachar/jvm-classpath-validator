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
        Instant start = Instant.now();
        Map<String, Map<String, String>> targetsToJarEntriesToDigests = extractEntries(jars);
        Instant end = Instant.now();
        System.out.println("extractEntries:" + Duration.between(start, end));
        start = Instant.now();
        List<ClasspathCollision> classpathCollisions = computeCollisions(targetsToJarEntriesToDigests);
        end = Instant.now();
        System.out.println("computeCollisions:" + Duration.between(start, end));
        return classpathCollisions;
    }

    private static List<ClasspathCollision> computeCollisions(Map<String, Map<String, String>> targetsToJarEntriesToDigests) {
        Instant start = Instant.now();
        Set<Pair<String, String>> pairs = allTargetsPairsIn(targetsToJarEntriesToDigests.keySet());
        Instant end = Instant.now();
        System.out.println("allPairs:" + Duration.between(start, end));
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

    private static Set<Pair<String, String>> allTargetsPairsIn(Set<String> originalSet) {
        if (originalSet.isEmpty())
            return Collections.emptySet();
        String head = originalSet.stream().findFirst().get();
        Set<String> tail = originalSet.stream().skip(1).collect(Collectors.toSet());

        Set<Pair<String, String>> currentPairs = tail.stream().map(s -> new Pair<>(head, s)).collect(Collectors.toSet());

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
