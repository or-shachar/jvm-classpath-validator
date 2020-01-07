load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

junit_version = "4.13"

def dependencies():
    maven_install(
        artifacts = [
            "junit:junit:%s" % junit_version,
            "org.assertj:assertj-core:3.14.0",
        ],
        repositories = [
            "https://repo1.maven.org/maven2",
            "https://maven.google.com",
        ],
        maven_install_json = "//:maven_install.json",
    )
