package com.bazelbuild.java.classpath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class ClassPathValidatorTestingUtils {
    public static List<ClasspathValidatorJarInput> setupCollision(String filePath, String labelA, String labelB) throws IOException {
        DummyFileEntry jarEntryA = new DummyFileEntry(filePath, "Contents A");
        Path dummyJarPathA = prepareDummyJarWith(jarEntryA);
        ClasspathValidatorJarInput jarInputA = new ClasspathValidatorJarInput("//a", dummyJarPathA);
        DummyFileEntry jarEntryB = new DummyFileEntry(filePath, "Contents B");
        Path dummyJarPathB = prepareDummyJarWith(jarEntryB);
        ClasspathValidatorJarInput jarInputB = new ClasspathValidatorJarInput("//b", dummyJarPathB);

        return Arrays.asList(jarInputA, jarInputB);
    }

    public static Path prepareDummyJarWith(DummyFileEntry... entries) throws IOException {
        Path basePath = Files.createTempDirectory("classpath-entries");
        Path jarPath = basePath.resolve("tempJar.jar");
        JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarPath));
        for (DummyFileEntry e : entries) {
            addFile(jarOutputStream, e);
        }
        jarOutputStream.close();
        return jarPath;
    }

    private static void addFile(JarOutputStream jarOutputStream, DummyFileEntry e) throws IOException {
        JarEntry aJarEntry = new JarEntry(e.path);
        byte[] content = e.content.getBytes();
        aJarEntry.setSize(content.length);
        jarOutputStream.setMethod(ZipEntry.DEFLATED);
        jarOutputStream.putNextEntry(aJarEntry);
        jarOutputStream.write(content);
        jarOutputStream.closeEntry();
    }
}