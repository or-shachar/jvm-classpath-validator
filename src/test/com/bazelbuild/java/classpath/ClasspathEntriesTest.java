package com.bazelbuild.java.classpath;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
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
        DummyFileEntry jarEntry = new DummyFileEntry("a.txt", "I am A");
        Path dummyJarPath = prepareDummyJarWith(jarEntry);

        List<MiniJarEntry> entries = ClasspathEntries.getEntries(dummyJarPath);

        MiniJarEntry expected = new MiniJarEntry(jarEntry.path, getDigest(jarEntry.content));
        System.out.println(expected);
        assertThat(entries).usingFieldByFieldElementComparator().containsExactly(expected);
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
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(context.getBytes());
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }


}