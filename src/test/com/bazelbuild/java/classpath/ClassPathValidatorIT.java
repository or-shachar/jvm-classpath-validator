package com.bazelbuild.java.classpath;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.bazelbuild.java.classpath.ClassPathValidatorTestingUtils.prepareDummyJarWith;
import static com.bazelbuild.java.classpath.ClassPathValidatorTestingUtils.setupCollision;
import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings("DuplicatedCode")
@RunWith(JUnit4.class)
public class ClassPathValidatorIT {

    ClassPathValidator validator = new ClassPathValidator(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

    @Test
    public void returnEmptyCollisionIfNoRecordWithSamePathWasFound() throws IOException, NoSuchAlgorithmException {
        DummyFileEntry jarEntryA = new DummyFileEntry("a.txt", "I am A");
        Path dummyJarPathA = prepareDummyJarWith(jarEntryA);
        ClasspathValidatorJarInput jarInputA = new ClasspathValidatorJarInput("//a", dummyJarPathA);

        DummyFileEntry jarEntryB = new DummyFileEntry("b.txt", "I am B");
        Path dummyJarPathB = prepareDummyJarWith(jarEntryB);
        ClasspathValidatorJarInput jarInputB = new ClasspathValidatorJarInput("//b", dummyJarPathB);

        List<ClasspathCollision> collisions = validator.collisionsIn(Arrays.asList(jarInputA,jarInputB));

        assertThat(collisions).isEmpty();
    }

    @Test
    public void returnEmptyCollisionIfSamePathExistButWithExactSameContent() throws IOException, NoSuchAlgorithmException {
        DummyFileEntry jarEntry = new DummyFileEntry("a.txt", "I am A");
        Path dummyJarPathA = prepareDummyJarWith(jarEntry);
        ClasspathValidatorJarInput jarInputA = new ClasspathValidatorJarInput("//a",dummyJarPathA);
        Path dummyJarPathB = prepareDummyJarWith(jarEntry);
        ClasspathValidatorJarInput jarInputB = new ClasspathValidatorJarInput("//b",dummyJarPathB);

        List<ClasspathCollision> collisions = validator.collisionsIn(Arrays.asList(jarInputA,jarInputB));

        assertThat(collisions).isEmpty();
    }

    @Test
    public void findCollisionIfSamePathFoundWithDifferentContent() throws IOException, NoSuchAlgorithmException {
        List<ClasspathCollision> collisions = validator.collisionsIn(setupCollision("file.txt", "//a", "//b"));

        ClasspathCollision expected = new ClasspathCollision("//a", "//b", Arrays.asList("file.txt"));
        assertThat(collisions).usingFieldByFieldElementComparator().contains(expected);
    }

    @Test
    public void ignoreCollisionIfPrefixIsNotIncluded() throws IOException {
        ClassPathValidator validator = new ClassPathValidator(
                Collections.emptyList(), Collections.emptyList(),
                Arrays.asList("not-file.txt"), Collections.emptyList()
        );

        List<ClasspathCollision> collisions = validator.collisionsIn(setupCollision("file.txt", "//a", "//b"));
        assertThat(collisions).isEmpty();
    }

    @Test
    public void ignoreCollisionIfSuffixIsNotIncluded() throws IOException {
        ClassPathValidator validator = new ClassPathValidator(
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Arrays.asList(".md")
        );

        List<ClasspathCollision> collisions = validator.collisionsIn(setupCollision("file.txt", "//a", "//b"));
        assertThat(collisions).isEmpty();
    }

    @Test
    public void collisionsCapturedIfPrefixMatch() throws IOException {
        ClassPathValidator validator = new ClassPathValidator(
                Collections.emptyList(), Collections.emptyList(),
                Arrays.asList("file"), Collections.emptyList()
        );

        List<ClasspathCollision> collisions = validator.collisionsIn(setupCollision("file.txt", "//a", "//b"));
        ClasspathCollision expected = new ClasspathCollision("//a", "//b", Arrays.asList("file.txt"));

        assertThat(collisions).usingFieldByFieldElementComparator().contains(expected);
    }

    @Test
    public void collisionsCapturedIfSuffixMatch() throws IOException {
        ClassPathValidator validator = new ClassPathValidator(
                Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Arrays.asList(".txt")
        );

        List<ClasspathCollision> collisions = validator.collisionsIn(setupCollision("file.txt", "//a", "//b"));
        ClasspathCollision expected = new ClasspathCollision("//a", "//b", Arrays.asList("file.txt"));

        assertThat(collisions).usingFieldByFieldElementComparator().contains(expected);
    }

    @Test
    public void ignorePrefixesTakePrecendenceOverIncludePrefixes() throws IOException {
        ClassPathValidator validator = new ClassPathValidator(
                Arrays.asList("file"), Collections.emptyList(),
                Arrays.asList("file"), Collections.emptyList()
        );

        List<ClasspathCollision> collisions = validator.collisionsIn(setupCollision("file.txt", "//a", "//b"));

        assertThat(collisions).isEmpty();
    }

    @Test
    public void ignoreSuffixesTakePrecendenceOverIncludeSuffixes() throws IOException {
        ClassPathValidator validator = new ClassPathValidator(
                Collections.emptyList(), Arrays.asList(".txt"),
                Collections.emptyList(), Arrays.asList(".txt")
        );

        List<ClasspathCollision> collisions = validator.collisionsIn(setupCollision("file.txt", "//a", "//b"));

        assertThat(collisions).isEmpty();
    }
}
