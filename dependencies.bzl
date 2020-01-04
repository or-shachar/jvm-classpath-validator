load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")

def dependencies():
    maven_install(
        artifacts = [
            "junit:junit:4.13",
            "org.assertj:assertj-core:3.14.0"
        ],
        repositories = [
            "https://maven.google.com",
            "https://repo1.maven.org/maven2",
        ],
        maven_install_json = "//:maven_install.json",
    )

