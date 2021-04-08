package com.bazelbuild.java.classpath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ClasspathValidatorCli {
    public static void main(String[] args) throws IOException {
        System.out.println("====================");
        System.out.println("Classpath Collision Finder");
        System.out.println("====================");

        ClasspathValidatorArguments cliArgs = new ClasspathValidatorArguments(args);

        String labelsToJarsPathsFile = cliArgs.getJarTargets();
        List<String> labelsToJarsPaths = readInputLines(labelsToJarsPathsFile);

        List<String> ignorePrefixes = cliArgs.getIgnorePrefix();
        List<String> ignoreSuffixes = cliArgs.getIgnoreSuffix();
        List<String> includePrefixes = cliArgs.getIncludePrefix();
        List<String> includeSuffixes = cliArgs.getIncludeSuffix();

        ClassPathValidator validator = new ClassPathValidator(ignorePrefixes, ignoreSuffixes, includePrefixes, includeSuffixes);
        List<ClasspathValidatorJarInput> jars = labelsToJarsPaths.stream()
                .map(ClasspathValidatorCli::toInput)
                .collect(Collectors.toList());
        System.out.println(String.format("[INFO]\tLooking for collisions in %s jar files", jars.size()));
        List<ClasspathCollision> collisions = validator.collisionsIn(jars);

        String exceptionCountPrint = String.format("Found %s collisions", collisions.size());
        System.out.println("=======");
        System.out.println(exceptionCountPrint);
        System.out.println("=======");
        printCollisions(collisions);

        if (!collisions.isEmpty()) {
            throw new RuntimeException(exceptionCountPrint);
        }
    }

    private static void printCollisions(List<ClasspathCollision> collisions) {
        for (ClasspathCollision c : collisions) {
            System.out.println(String.format("[%s]:", c.targets));
            c.differentEntries.forEach(e -> System.out.println("\t " + e));
            System.out.println();
        }
    }

    private static ClasspathValidatorJarInput toInput(String line) {
        String[] parts = line.split(" ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Illegal line in jars file: " + line);
        }
        String label = parts[0];
        Path jarPath = asReadablePath(parts[1]);
        return new ClasspathValidatorJarInput(label, jarPath);
    }

    private static Path asReadablePath(String pathString) {
        Path path = Paths.get(pathString);
        if (!Files.isReadable(path)) {
            String error = String.format("File %s is not readable", pathString);
            System.err.println("[ERROR]\t" + error);
            throw new IllegalArgumentException(error);
        }
        return path;
    }

    private static List<String> readInputLines(String filePath) throws IOException {
        Path targetsToJarsFile = asReadablePath(filePath);
        return Files.readAllLines(targetsToJarsFile).stream()
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .filter(s -> !s.startsWith("#"))
            .collect(Collectors.toList());
    }
}
