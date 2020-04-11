# What is this folder?

This is just for testing. Probably better if we use [bazel-integration-testing](https://github.com/bazelbuild/bazel-integration-testing) lib.

To test behavior run:
```sh
bazel test //example/c/com/example/consumer:test_classpath_should_fail
```

This target is excluded by default by the `manual` tag since it's purposely failing
