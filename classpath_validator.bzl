def _label_as_string(label):
    return "{workspace_name}//{package}:{name}".format(
        workspace_name = "" if (not label.workspace_name) else "@" + label.workspace_name,
        package = label.package,
        name = label.name,
    )

def _construct_args(args, argument_name, values):
    for value in values:
        args.append(
            '''"{0}={1}"'''.format(argument_name, value)
        )

def _impl(ctx):
    target = ctx.attr.target
    runtime_jars = target[java_common.provider].transitive_runtime_jars.to_list()

    jars = [(_label_as_string(j.owner) + " " + j.short_path) for j in runtime_jars]

    arguments = []
    _construct_args(arguments, "--ignore-prefix", ctx.attr.ignore_prefixes)
    _construct_args(arguments, "--ignore-suffix", ctx.attr.ignore_suffixes)
    _construct_args(arguments, "--include-prefix", ctx.attr.include_prefixes)
    _construct_args(arguments, "--include-suffix", ctx.attr.include_suffixes)
    _construct_args(arguments, "--jar-targets", jars)

    # generate executable command
    validator_exectuable = ctx.attr._validator[DefaultInfo].files_to_run.executable.short_path
    cmd = "{validator_exectuable} {arguments}".format(
        validator_exectuable = validator_exectuable,
        arguments = " ".join(arguments),
    )

    exec = ctx.actions.declare_file(ctx.label.name + "_test_run.sh")
    ctx.actions.write(
        output = exec,
        content = cmd,
        is_executable = True,
    )

    # compute runfiles
    runfiles = ctx.runfiles(files = [exec,] + runtime_jars) \
        .merge(ctx.attr._validator[DefaultInfo].default_runfiles)

    return [DefaultInfo(executable = exec, runfiles = runfiles)]

classpath_collision_test = rule(
    implementation = _impl,
    attrs = {
        "target": attr.label(providers = [JavaInfo]),
        "ignore_prefixes": attr.string_list(doc = "prefixes of jar entries to ignore", default = []),
        "ignore_suffixes": attr.string_list(doc = "suffixes of jar entries to ignore", default = []),
        "include_prefixes": attr.string_list(doc = "prefixes of jar entries to check", default = []),
        "include_suffixes": attr.string_list(doc = "suffixes of jar entries to check", default = []),
        "_validator": attr.label(providers = [DefaultInfo], default = "//src/main/com/bazelbuild/java/classpath:classpath_run"),
    },
    test = True,
)
