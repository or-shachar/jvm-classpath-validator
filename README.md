# Bazel classpath_collision_test [![Build Status](https://api.cirrus-ci.com/github/or-shachar/jvm-classpath-validator.svg)](https://cirrus-ci.com/github/or-shachar/jvm-classpath-validator/master)


## Overview
Test and report tool to validate classpath collision cases

## Getting started
1. Add to your WORKSPACE file the following:
    ```python
    # WORKSPACE file
    load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
    
    jvm_classpath_validator_version = "16101cfd51a249bc120dc9be9e70eef42faf4745" # update this as needed
    jvm_classpath_validator_sha256 = "c0d0bf966575ac7333fd21319ad977291551bc2060ea3cd09c525469f2c59f2b" # update this as needed
   
    http_archive(
        name = "jvm_classpath_validator",
        url = "https://github.com/or-shachar/jvm-classpath-validator/archive/%s.tar.gz" % jvm_classpath_validator_version,
        strip_prefix = "jvm-classpath-validator-%s" % jvm_classpath_validator_version,
        sha256 = jvm_classpath_validator_sha256
    )
    ```
2. Add a test in one of the build files:
    ```python
    # BUILD.bazel file
    load("@jvm_classpath_validator//:classpath_validator.bzl", "classpath_collision_test")
   
    java_binary(
        name = "foo",
        runtime_deps = [...]
    )
   
    classpath_collision_test(
        name = "test_classpath",
        target = "foo",
        ignore_prefixes = ["example_prefix"], #optional
        ignore_suffixes = ["example_suffix"], #optional
        include_prefixes = ["example_prefix"], #optional
        include_suffixes = ["example_suffix"], #optional
    )
    ```
3. Run `bazel test //path/to/package:test_classpath`

### How does the test work?
* The test would inspect the different jar entries of any jar in runtime closure of given `target`
* The test would ignore entries with given prefixes or suffix and look only at entries that match the provided
prefixes or suffixes (or all entries if the `include_prefix` and `include_suffixes` are not provided)
* If same entry was found in two places with different content (digest based) the test would fail and a report would be emitted.


## Example for failure report
```txt
Classpath Collision Finder
====================
[INFO]	Looking for collisions in 3 jar files
=======
Found 1 collisions

[//example/a/com/example/duppy:duppy <> //example/b/com/example/duppy:duppy]:
	 com/example/duppy/Duppy.class

```

##ignore_ vs include_ parameters
The test will perform the filtering out of all entries from JARs before looking at what to include, i.e.
if an entry from a JAR matches both `ignore_` and `include_` pattern, then `ignore_` will take precedence.

## Default ignore list
I allowed myself to add few default prefixes / suffixes to ignore.
You can find them [here](https://github.com/or-shachar/jvm-classpath-validator/blob/master/src/main/com/bazelbuild/java/classpath/ClassPathValidator.java).
If you feel like the defaults should change - please kindly open an issue.

