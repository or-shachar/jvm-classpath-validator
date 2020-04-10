package com.bazelbuild.java.classpath;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import static com.bazelbuild.java.classpath.ClassPathValidatorTestingUtils.prepareDummyJarWith;
import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings("DuplicatedCode")
@RunWith(JUnit4.class)
public class ClassPathValidatorIT {
    @Test
    public void returnEmptyCollisionIfNoRecordWithSamePathWasFound() throws IOException, NoSuchAlgorithmException {
        DummyFileEntry jarEntryA = new DummyFileEntry("a.txt", "I am A");
        Path dummyJarPathA = prepareDummyJarWith(jarEntryA);
        ClasspathValidatorJarInput jarInputA = new ClasspathValidatorJarInput("//a",dummyJarPathA);
        DummyFileEntry jarEntryB = new DummyFileEntry("b.txt", "I am B");
        Path dummyJarPathB = prepareDummyJarWith(jarEntryB);
        ClasspathValidatorJarInput jarInputB = new ClasspathValidatorJarInput("//b",dummyJarPathB);

        List<ClasspathCollision> collisions = ClassPathValidator.collisionsIn(Arrays.asList(jarInputA,jarInputB));

        assertThat(collisions).isEmpty();
    }

    @Test
    public void returnEmptyCollisionIfSamePathExistButWithExactSameContent() throws IOException, NoSuchAlgorithmException {
        DummyFileEntry jarEntry = new DummyFileEntry("a.txt", "I am A");
        Path dummyJarPathA = prepareDummyJarWith(jarEntry);
        ClasspathValidatorJarInput jarInputA = new ClasspathValidatorJarInput("//a",dummyJarPathA);
        Path dummyJarPathB = prepareDummyJarWith(jarEntry);
        ClasspathValidatorJarInput jarInputB = new ClasspathValidatorJarInput("//b",dummyJarPathB);

        List<ClasspathCollision> collisions = ClassPathValidator.collisionsIn(Arrays.asList(jarInputA,jarInputB));

        assertThat(collisions).isEmpty();
    }

    @Test
    public void findCollisionIfSamePathFoundWithDifferentContent() throws IOException, NoSuchAlgorithmException {
        String samePath = "file.txt";
        DummyFileEntry jarEntryA = new DummyFileEntry(samePath, "I am A");
        Path dummyJarPathA = prepareDummyJarWith(jarEntryA);
        ClasspathValidatorJarInput jarInputA = new ClasspathValidatorJarInput("//a",dummyJarPathA);
        DummyFileEntry jarEntryB = new DummyFileEntry(samePath, "I am B");
        Path dummyJarPathB = prepareDummyJarWith(jarEntryB);
        ClasspathValidatorJarInput jarInputB = new ClasspathValidatorJarInput("//b",dummyJarPathB);

        List<ClasspathCollision> collisions = ClassPathValidator.collisionsIn(Arrays.asList(jarInputA,jarInputB));

        ClasspathCollision expected = new ClasspathCollision(jarInputA.label,jarInputB.label,Arrays.asList(samePath));
        assertThat(collisions).usingFieldByFieldElementComparator().contains(expected);
    }
}
