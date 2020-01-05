package com.bazelbuild.java.classpath;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ClasspathEntries {
    public static List<MiniJarEntry> getEntries(Path dummyJarPath) throws IOException {
        JarInputStream inputStream = new JarInputStream(Files.newInputStream(dummyJarPath));
        JarEntry entry;
        List<MiniJarEntry> res = new ArrayList<>();
        while ((entry = inputStream.getNextJarEntry()) != null){
            byte[] bytes = readEntry(inputStream);
            res.add(new MiniJarEntry(entry.getName(),getDigest(bytes)));
        }
        return Collections.unmodifiableList(res);
    }

    private static byte[] readEntry(JarInputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }

    public static String getDigest(byte[] content)  {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Could not get SHA-256 algorithm, should not happen. Goodbye");
            System.exit(1);
            return null;
        }
    }
}
