package com.bazelbuild.java.classpath;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

        String[] args = getJarTargetsParameter(jarInputA, jarInputB);

        Throwable thrown = catchThrowable(() -> ClasspathValidatorCli.main(args));

        assertThat(thrown).isInstanceOf(RuntimeException.class).hasMessageContaining("1 collisions");
    }

    @Test
    public void throwIllegalArgumentForNonReadableJarsFilePath() throws IOException {
        Path inputFile = Files.createTempDirectory("my-temp").resolve("non-existing.txt");
        String[] args = {"--jar-targets=" + inputFile.toString()};
        Throwable thrown = catchThrowable(() -> ClasspathValidatorCli.main(args));

        assertThat(thrown).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Illegal line in jars file");
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

        String[] args = getJarTargetsParameter(jarInputA, jarInputB);
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

        String[] args = getJarTargetsParameter(jarInputA, jarInputB);
        Throwable thrown = catchThrowable(() -> ClasspathValidatorCli.main(args));

        assertThat(thrown).isNull();
    }

    private String[] getJarTargetsParameter(ClasspathValidatorJarInput ...jarInputs) {
        List<String> args = new ArrayList<>();

        for (ClasspathValidatorJarInput jarInput: jarInputs) {
            args.add(String.format("--jar-targets=%s %s", jarInput.label, jarInput.jarPath.toString()));
        }

        return args.toArray(new String[args.size()]);
    }
}