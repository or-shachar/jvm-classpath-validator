package com.bazelbuild.java.classpath;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassPathValidator {
    private static List<String> prefixIgnoreDefaults = Arrays.asList("META-INF");
    private static List<String> suffixIgnoreDefaults = Arrays.asList("LICENSE", "pom.xml", "BUILD.bazel", "module-info.class", "LICENSE.txt", "NOTICE", "mozilla/public-suffix-list.txt", "rootdoc.txt");

    private final List<String> ignorePrefixes;
    private final List<String> ignoreSuffixes;
    private final List<String> includePrefixes;
    private final List<String> includeSuffixes;

    public ClassPathValidator(List<String> ignorePrefixes, List<String> ignoreSuffixes, List<String> includePrefixes, List<String> includeSuffixes) {
        this.ignorePrefixes = Stream.concat(ignorePrefixes.stream(), prefixIgnoreDefaults.stream()).collect(Collectors.toList());
        this.ignoreSuffixes = Stream.concat(ignoreSuffixes.stream(), suffixIgnoreDefaults.stream()).collect(Collectors.toList());
        this.includePrefixes = includePrefixes;
        this.includeSuffixes = includeSuffixes;
    }

    List<ClasspathCollision> collisionsIn(List<ClasspathValidatorJarInput> jars) throws IOException {
        Map<String, Map<String, String>> targetsToJarEntriesToDigests = extractEntries(jars);
        return computeCollisions(targetsToJarEntriesToDigests);
    }

    private List<ClasspathCollision> computeCollisions(Map<String, Map<String, String>> targetsToJarEntriesToDigests) {
        List<TargetsPair> pairs = allTargetsPairsIn(new ArrayList<>(targetsToJarEntriesToDigests.keySet()));
        return pairs.stream()
                .map(p -> collisionsBetween(targetsToJarEntriesToDigests, p))
                .filter(c -> !c.differentEntries.isEmpty())
                .collect(Collectors.toList());
    }

    private ClasspathCollision collisionsBetween(Map<String, Map<String, String>> targetsToJarEntriesToDigests,
                                                 TargetsPair targetsPair) {
        String target1 = targetsPair.target1;
        String target2 = targetsPair.target2;
        List<String> collisions = findCollisionsIn(targetsToJarEntriesToDigests.get(target1), targetsToJarEntriesToDigests.get(target2));
        return new ClasspathCollision(target1, target2, collisions);
    }

    private List<String> findCollisionsIn(Map<String, String> jar1EntriesToDigest, Map<String, String> jar2EntriesToDigest) {
        return jar1EntriesToDigest.keySet().stream().filter(f ->
                jar2EntriesToDigest.containsKey(f) && !jar1EntriesToDigest.get(f).equals(jar2EntriesToDigest.get(f))
        ).collect(Collectors.toList());
    }

    private List<TargetsPair> allTargetsPairsIn(List<String> originalSet) {
        if (originalSet.isEmpty())
            return Collections.emptyList();
        String head = originalSet.get(0);
        List<String> tail = originalSet.subList(1, originalSet.size());
        List<TargetsPair> currentPairs = tail.stream().map(s -> new TargetsPair(head, s)).collect(Collectors.toList());
        currentPairs.addAll(allTargetsPairsIn(tail));
        return currentPairs;
    }


    private Map<String, Map<String, String>> extractEntries(List<ClasspathValidatorJarInput> jars) throws IOException {
        Map<String, Map<String, String>> labelToEntriesMap = new HashMap<>();
        for (ClasspathValidatorJarInput j : jars) {
            List<MiniJarEntry> jarEntries = ClasspathEntries.getEntries(j.jarPath);
            Map<String, String> classpathToDigest = new HashMap<>();
            jarEntries.stream()
                    .filter(g -> !ignored(g.path))
                    .filter(g -> included(g.path))
                    .forEach(e-> classpathToDigest.put(e.getPath(),e.getDigest()));
            labelToEntriesMap.put(j.label, classpathToDigest);
        }
        return Collections.unmodifiableMap(labelToEntriesMap);
    }

    private boolean ignored(String path) {
        return matchSuffixOrPrefix(path, this.ignorePrefixes, this.ignoreSuffixes);
    }

    private boolean included(String path) {
        // If no includes are specified, we include everything
        if (this.includePrefixes.isEmpty() && this.includeSuffixes.isEmpty()) {
            return true;
        }

        return matchSuffixOrPrefix(path, this.includePrefixes, this.includeSuffixes);
    }

    private boolean matchSuffixOrPrefix(String path, List<String> prefixes, List<String> suffixes) {
        return prefixes.stream().anyMatch(path::startsWith) ||
                suffixes.stream().anyMatch(path::endsWith);
    }

    private class TargetsPair{
        final String target1;
        final String target2;

        private TargetsPair(String target1, String target2) {
            this.target1 = target1;
            this.target2 = target2;
        }
    }
}
