package com.bazelbuild.java.classpath;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.bazelbuild.java.classpath.ClassPathValidatorTestingUtils.prepareDummyJarWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@SuppressWarnings("ALL")
@RunWith(JUnit4.class)
public class ClasspathValidatorCliIT {
    @Test
    public void throwsRuntimeExceptionInCaseItFoundCollision() throws IOException {
        String samePath = "file.txt";
        DummyFileEntry jarEntryA = new DummyFileEntry(samePath, "I am A");
        Path dummyJarPathA = prepareDummyJarWith(jarEntryA);
        ClasspathValidatorJarInput jarInputA = new ClasspathValidatorJarInput("//a",dummyJarPathA);

        DummyFileEntry jarEntryB = new DummyFileEntry(samePath, "I am B");
        Path dummyJarPathB = prepareDummyJarWith(jarEntryB);
        ClasspathValidatorJarInput jarInputB = new ClasspathValidatorJarInput("//b",dummyJarPathB);

        String[] args = createArgs(jarInputA, jarInputB);

        Throwable thrown = catchThrowable(() -> ClasspathValidatorCli.main(args));

        assertThat(thrown).isInstanceOf(RuntimeException.class).hasMessageContaining("1 collisions");
    }

    @Test
    public void throwIllegalArgumentForNonReadableJarsFilePath() throws IOException {
        Path inputFile = Files.createTempDirectory("my-temp").resolve("non-existing.txt");
        String[] args = {"--jar-targets=" + inputFile.toString()};
        Throwable thrown = catchThrowable(() -> ClasspathValidatorCli.main(args));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("is not readable");
    }

    @Test
    public void throwIllegalArgumentForBadlyFormattedFiles() throws IOException {
        Path inputFile = Files.createTempFile("jarFiles",".txt");
        Files.write(inputFile, "This is not formatted correctly".getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE);
        String[] args = {"--jar-targets=" + inputFile.toString()};

        Throwable thrown = catchThrowable(() -> ClasspathValidatorCli.main(args));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("llegal line in jars file");
    }

    @Test
    public void throwIllegalArgumentIfAnyJarPathIsMissing() throws IOException {
        String samePath = "file.txt";
        DummyFileEntry jarEntryA = new DummyFileEntry(samePath, "I am A");
        Path dummyJarPathA = prepareDummyJarWith(jarEntryA);
        ClasspathValidatorJarInput jarInputA = new ClasspathValidatorJarInput("//a",dummyJarPathA);
        DummyFileEntry jarEntryB = new DummyFileEntry(samePath, "I am B");
        Path dummyJarPathB = prepareDummyJarWith(jarEntryB);
        Files.delete(dummyJarPathB);
        ClasspathValidatorJarInput jarInputB = new ClasspathValidatorJarInput("//b",dummyJarPathB);

        String[] args = createArgs(jarInputA, jarInputB);
        Throwable thrown = catchThrowable(() -> ClasspathValidatorCli.main(args));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("is not readable");
    }

    @Test
    public void exitWithoutErrorsInCaseNoCollisionsFound() throws IOException{
        DummyFileEntry jarEntryA = new DummyFileEntry("a.txt", "I am A");
        Path dummyJarPathA = prepareDummyJarWith(jarEntryA);
        ClasspathValidatorJarInput jarInputA = new ClasspathValidatorJarInput("//a",dummyJarPathA);
        DummyFileEntry jarEntryB = new DummyFileEntry("b.txt", "I am B");
        Path dummyJarPathB = prepareDummyJarWith(jarEntryB);
        ClasspathValidatorJarInput jarInputB = new ClasspathValidatorJarInput("//b",dummyJarPathB);

        String[] args = createArgs(jarInputA, jarInputB);
        Throwable thrown = catchThrowable(() -> ClasspathValidatorCli.main(args));

        assertThat(thrown).isNull();
    }

    private String[] createArgs(ClasspathValidatorJarInput... jarsInput) throws IOException {
        Path inputFile = Files.createTempFile("jarFiles",".txt");

        for (ClasspathValidatorJarInput input:jarsInput) {
            String line = String.format("%s %s\n", input.label, input.jarPath.toString());
            Files.write(inputFile, line.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }

        return new String[]{
            String.format("--jar-targets=%s", inputFile.toString())
        };
    }
}