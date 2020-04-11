# Bazel classpath_collision_test


## Overview
Test and report tool to validate classpath collision cases

## Getting started
1. Add to your WORKSPACE file the following:
    ```python
    # WORKSPACE file
    load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
    
    jvm_classpath_validator_version="78eff5bf1dac40477b565e45a0ca83566ed5fd30" # update this as needed
    jvm_classpath_validator_sha256="e1ea8c339f864b80e80e4e31785771332ffadf6754a06d860d43ab9c7622b4a8" #update this as needed
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
