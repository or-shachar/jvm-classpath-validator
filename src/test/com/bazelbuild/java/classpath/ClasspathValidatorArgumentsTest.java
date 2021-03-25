package com.bazelbuild.java.classpath;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class ClasspathValidatorArgumentsTest {

    @Test
    public void defaultValuesAreEmpty() {
        ClasspathValidatorArguments args = new ClasspathValidatorArguments(new String[]{});

        argsAreEmpty(args);
    }

    @Test
    public void ignoresUnrecognisedArgs() {
        ClasspathValidatorArguments args = new ClasspathValidatorArguments(new String[]{
            "--not-recognised",
            "123",
            "--help",
            "--version",
        });

        argsAreEmpty(args);
    }

    @Test
    public void notSpecifyingValuesIsAllowed() {
        ClasspathValidatorArguments args = new ClasspathValidatorArguments(new String[]{
            "--ignore-prefix",
            "--ignore-suffix",
            "--include-prefix",
            "--include-suffix",
        });

        argsAreEmpty(args);

        args = new ClasspathValidatorArguments(new String[]{
            "--ignore-prefix=",
            "--ignore-suffix=",
            "--include-prefix=",
            "--include-suffix=",
        });

        argsAreEmpty(args);
    }

    @Test
    public void canSetIgnorePrefix() {
        ClasspathValidatorArguments args = new ClasspathValidatorArguments(new String[]{
            "--ignore-prefix=prefix1",
            "--ignore-prefix=prefix2",
            "--ignore-prefix=prefix3",
        });

        assertThat(args.getIgnorePrefix()).isEqualTo(Arrays.asList("prefix1", "prefix2", "prefix3"));
        assertThat(args.getIgnoreSuffix()).isEmpty();
        assertThat(args.getIncludePrefix()).isEmpty();
        assertThat(args.getIncludeSuffix()).isEmpty();
        assertThat(args.getJarTargets()).isEmpty();
    }

    @Test
    public void canSetIgnoreSuffix() {
        ClasspathValidatorArguments args = new ClasspathValidatorArguments(new String[]{
            "--ignore-suffix=suffix1",
            "--ignore-suffix=suffix2",
            "--ignore-suffix=suffix3",
        });

        assertThat(args.getIgnorePrefix()).isEmpty();
        assertThat(args.getIgnoreSuffix()).isEqualTo(Arrays.asList("suffix1", "suffix2", "suffix3"));
        assertThat(args.getIncludePrefix()).isEmpty();
        assertThat(args.getIncludeSuffix()).isEmpty();
        assertThat(args.getJarTargets()).isEmpty();
    }

    @Test
    public void canSetIncludePrefix() {
        ClasspathValidatorArguments args = new ClasspathValidatorArguments(new String[]{
            "--include-prefix=prefix1",
            "--include-prefix=prefix2",
            "--include-prefix=prefix3",
        });

        assertThat(args.getIgnorePrefix()).isEmpty();
        assertThat(args.getIgnoreSuffix()).isEmpty();
        assertThat(args.getIncludePrefix()).isEqualTo(Arrays.asList("prefix1", "prefix2", "prefix3"));
        assertThat(args.getIncludeSuffix()).isEmpty();
        assertThat(args.getJarTargets()).isEmpty();
    }

    @Test
    public void canSetIncludeSuffix() {
        ClasspathValidatorArguments args = new ClasspathValidatorArguments(new String[]{
            "--include-suffix=suffix1",
            "--include-suffix=suffix2",
            "--include-suffix=suffix3",
        });

        assertThat(args.getIgnorePrefix()).isEmpty();
        assertThat(args.getIgnoreSuffix()).isEmpty();
        assertThat(args.getIncludePrefix()).isEmpty();
        assertThat(args.getIncludeSuffix()).isEqualTo(Arrays.asList("suffix1", "suffix2", "suffix3"));
        assertThat(args.getJarTargets()).isEmpty();
    }

    @Test
    public void canSetJarTargets() {
        ClasspathValidatorArguments args = new ClasspathValidatorArguments(new String[]{
            "--jar-targets=target1",
            "--jar-targets=target2",
            "--jar-targets=target3",
        });

        assertThat(args.getIgnorePrefix()).isEmpty();
        assertThat(args.getIgnoreSuffix()).isEmpty();
        assertThat(args.getIncludePrefix()).isEmpty();
        assertThat(args.getIncludeSuffix()).isEmpty();
        assertThat(args.getJarTargets()).isEqualTo(Arrays.asList("target1", "target2", "target3"));
    }

    @Test
    public void canSetAllParameters() {
        ClasspathValidatorArguments args = new ClasspathValidatorArguments(new String[]{
            "--ignore-prefix=prefix1",
            "--ignore-suffix=suffix1",
            "--include-prefix=prefix2",
            "--include-suffix=suffix2",
            "--jar-targets=target1",
        });

        assertThat(args.getIgnorePrefix()).isEqualTo(Arrays.asList("prefix1"));
        assertThat(args.getIgnoreSuffix()).isEqualTo(Arrays.asList("suffix1"));
        assertThat(args.getIncludePrefix()).isEqualTo(Arrays.asList("prefix2"));
        assertThat(args.getIncludeSuffix()).isEqualTo(Arrays.asList("suffix2"));
        assertThat(args.getJarTargets()).isEqualTo(Arrays.asList("target1"));
    }

    private void argsAreEmpty(ClasspathValidatorArguments args) {
        assertThat(args.getIgnorePrefix()).isEmpty();
        assertThat(args.getIgnoreSuffix()).isEmpty();
        assertThat(args.getIncludePrefix()).isEmpty();
        assertThat(args.getIncludeSuffix()).isEmpty();
        assertThat(args.getJarTargets()).isEmpty();
    }
}
