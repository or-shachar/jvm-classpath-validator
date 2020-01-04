package com.bazelbuild.java.classpath;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class ClasspathEntriesTest {
    @Test
    public void listFiles() throws IOException, NoSuchAlgorithmException {
        DummyFileEntry jarEntryA = new DummyFileEntry("a.txt", "I am A");
        DummyFileEntry jarEntryB = new DummyFileEntry("b.txt", "I am B");
        Path dummyJarPath = prepareDummyJarWith(jarEntryA,jarEntryB);

        List<MiniJarEntry> entries = ClasspathEntries.getEntries(dummyJarPath);

        MiniJarEntry expectedA = new MiniJarEntry(jarEntryA.path, getDigest(jarEntryA.content));
        MiniJarEntry expectedB = new MiniJarEntry(jarEntryB.path, getDigest(jarEntryB.content));
        assertThat(entries).usingFieldByFieldElementComparator().containsExactly(expectedA,expectedB);
    }

    private Path prepareDummyJarWith(DummyFileEntry... entries) throws IOException {
        Path basePath = Files.createTempDirectory("classpath-entries");
        Path jarPath = basePath.resolve("tempJar.jar");
        JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(jarPath));
        for (DummyFileEntry e : entries) {
            addFile(jarOutputStream, e);
        }
        jarOutputStream.close();
        return jarPath;
    }

    private void addFile(JarOutputStream jarOutputStream, DummyFileEntry e) throws IOException {
        JarEntry aJarEntry = new JarEntry(e.path);
        byte[] content = e.content.getBytes();
        aJarEntry.setSize(content.length);
        jarOutputStream.setMethod(ZipEntry.DEFLATED);
        jarOutputStream.putNextEntry(aJarEntry);
        jarOutputStream.write(content);
        jarOutputStream.closeEntry();
    }

    private String getDigest(String context) throws NoSuchAlgorithmException {
        return ClasspathEntries.getDigest(context.getBytes());
    }


}