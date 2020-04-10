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

        Path inputFile = prepareInputFileWith(jarInputA,jarInputB);
        String[] args = {inputFile.toString()};
        Throwable thrown = catchThrowable(() -> ClasspathValidatorCli.main(args));

        assertThat(thrown).isInstanceOf(RuntimeException.class).hasMessageContaining("1 collisions");
    }

    @Test
    public void throwIllegalArgumentForNonReadableJarsFilePath() throws IOException {
        Path inputFile = Files.createTempDirectory("my-temp").resolve("non-existing.txt");
        String[] args = {inputFile.toString()};
        Throwable thrown = catchThrowable(() -> ClasspathValidatorCli.main(args));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("is not readable");
    }

    @Test
    public void throwIllegalArgumentForMissingArguments() throws IOException {
        String[] args = {};
        Throwable thrown = catchThrowable(() -> ClasspathValidatorCli.main(args));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Missing argument");
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

        Path inputFile = prepareInputFileWith(jarInputA,jarInputB);
        String[] args = {inputFile.toString()};
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

        Path inputFile = prepareInputFileWith(jarInputA,jarInputB);
        String[] args = {inputFile.toString()};
        Throwable thrown = catchThrowable(() -> ClasspathValidatorCli.main(args));

        assertThat(thrown).isNull();
    }

    private Path prepareInputFileWith(ClasspathValidatorJarInput... jarsInput) throws IOException {
        Path inputFile = Files.createTempFile("jarFiles",".txt");
        for(ClasspathValidatorJarInput input:jarsInput){
            String line = String.format("%s %s\n", input.label,input.jarPath.toString());
            Files.write(inputFile,line.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }
        return inputFile;
    }
}