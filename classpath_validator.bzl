def _label_as_string(label):
    return "{workspace_name}//{package}:{name}".format(
        workspace_name = "" if (not label.workspace_name) else "@" + label.workspace_name,
        package = label.package,
        name = label.name,
    )

def _impl(ctx):
    target = ctx.attr.target
    runtime_jars = target[java_common.provider].transitive_runtime_jars.to_list()

    # write argument file
    jars_file = ctx.actions.declare_file(ctx.label.name + "_jars.txt")
    ctx.actions.write(
        output = jars_file,
        content = "\n".join([(_label_as_string(j.owner) + " " + j.short_path) for j in runtime_jars]),
    )
    validator_runtime_jars = ctx.attr._validator[java_common.provider].transitive_runtime_jars.to_list()
    classpath = ":".join([j.short_path for j in validator_runtime_jars])
    java_runtime = ctx.attr._jdk[java_common.JavaRuntimeInfo]
    cmd = "{java} -cp {classpath} {main_class} {jar_files_path}\n".format(
        java = java_runtime.java_home + "/bin/java",
        main_class = "com.bazelbuild.java.classpath.ClasspathValidatorCli",
        classpath = classpath,
        jar_files_path = jars_file.short_path,
    )
    print(cmd)
    exec = ctx.actions.declare_file(ctx.label.name + "_test_run.sh")
    ctx.actions.write(
        output = exec,
        content = cmd,
        is_executable = True,
    )
    runfiles = ctx.runfiles(files = validator_runtime_jars + [exec, jars_file] + runtime_jars + ctx.files._jdk)
    return [DefaultInfo(executable = exec, runfiles = runfiles)]

classpath_collision_test = rule(
    implementation = _impl,
    attrs = {
        "target": attr.label(providers = [JavaInfo]),
        "_validator": attr.label(providers = [JavaInfo], default = "//src/main/com/bazelbuild/java/classpath"),
        "_jdk": attr.label(
            default = Label("@bazel_tools//tools/jdk:current_java_runtime"),
            providers = [java_common.JavaRuntimeInfo],
        ),
    },
    test = True,
)
