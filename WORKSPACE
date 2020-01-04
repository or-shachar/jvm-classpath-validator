workspace(name = "jvm_classpath_validator")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

bazel_federation_version = "b3252e9c00cd6c788139008cd0ce34a4b0481b53"
bazel_federation_sha256 = "67d5ac3b84342b562fe74abb2ea25f18408220a730fd647c32f4574aa8853b34"

http_archive(
    name = "bazel_federation",
    sha256 = bazel_federation_sha256,
    strip_prefix = "bazel-federation-%s" % bazel_federation_version,
    type = "zip",
    url = "https://github.com/bazelbuild/bazel-federation/archive/%s.zip" % bazel_federation_version,
)

load("@bazel_federation//:repositories.bzl", "rules_java")

rules_java()

load("@bazel_federation//setup:rules_java.bzl", "rules_java_setup")

rules_java_setup()


RULES_JVM_EXTERNAL_TAG = "3.1"
RULES_JVM_EXTERNAL_SHA = "e246373de2353f3d34d35814947aa8b7d0dd1a58c2f7a6c41cfeaff3007c2d14"

http_archive(
    name = "rules_jvm_external",
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    sha256 = RULES_JVM_EXTERNAL_SHA,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = [
        "junit:junit:4.13",
    ],
    repositories = [
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
    maven_install_json = "//:maven_install.json",
)

load("@maven//:defs.bzl", "pinned_maven_install")
pinned_maven_install()
