# Bazel classpath_collision_test [![Build Status](https://api.cirrus-ci.com/github/or-shachar/jvm-classpath-validator.svg)](https://cirrus-ci.com/github/or-shachar/jvm-classpath-validator/master)


## Overview
Test and report tool to validate classpath collision cases

## Getting started
1. Add to your WORKSPACE file the following:
    ```python
    # WORKSPACE file
    load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
    
    jvm_classpath_validator_version="c2656cbc6798e558414b17ffac01c9734d645910" # update this as needed
    jvm_classpath_validator_sha256="577e538c1ccdf3b3828abf7cd67fe93b58190cad26a8f81c12a5aa9f24a2f029" # update this as needed
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
    )
    ```
3. Run `bazel test //path/to:target:test_classpath`

### How does the test work?
* The test would inspect the different jar entries of any jar in runtime closure of given `target`
* The test would ignore entries with given suffix or prefixes. 
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


## Default ignore list
I allowed myself to add few default prefixes / suffixes to ignore.
You can find them [here](https://github.com/or-shachar/jvm-classpath-validator/blob/master/src/main/com/bazelbuild/java/classpath/ClassPathValidator.java).
If you feel like the defaults should change - please kindly open an issue.

