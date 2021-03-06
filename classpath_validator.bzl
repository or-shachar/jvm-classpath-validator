def _label_as_string(label):
    return "{workspace_name}//{package}:{name}".format(
        workspace_name = "" if (not label.workspace_name) else "@" + label.workspace_name,
        package = label.package,
        name = label.name,
    )

def _write_ignore_file(ctx, ignore_list,file_name):
    ignore_file = ctx.actions.declare_file(file_name)
    ctx.actions.write(
        output = ignore_file,
        content = "\n".join(ignore_list),
    )
    return ignore_file

def _impl(ctx):
    target = ctx.attr.target
    runtime_jars = target[java_common.provider].transitive_runtime_jars.to_list()

    # write argument file
    jars_file = ctx.actions.declare_file(ctx.label.name + "_jars.txt")
    ctx.actions.write(
        output = jars_file,
        content = "\n".join([(_label_as_string(j.owner) + " " + j.short_path) for j in runtime_jars]),
    )

    ignore_prefixes_file = _write_ignore_file(ctx, ctx.attr.ignore_prefixes, ctx.label.name + "_ignore_prefixes.txt")
    ignore_suffixes_file = _write_ignore_file(ctx, ctx.attr.ignore_suffixes, ctx.label.name + "_ignore_suffixes.txt")
    # generate executable command
    validator_exectuable = ctx.attr._validator[DefaultInfo].files_to_run.executable.short_path
    cmd = "{validator_exectuable} {jar_files_path} {ignore_prefixes_file} {ignore_suffixes_file}".format(
        validator_exectuable = validator_exectuable,
        jar_files_path = jars_file.short_path,
        ignore_prefixes_file = ignore_prefixes_file.short_path,
        ignore_suffixes_file = ignore_suffixes_file.short_path,
    )

    exec = ctx.actions.declare_file(ctx.label.name + "_test_run.sh")
    ctx.actions.write(
        output = exec,
        content = cmd,
        is_executable = True,
    )

    # compute runfiles
    runfiles = ctx.runfiles(files = [exec, jars_file, ignore_prefixes_file, ignore_suffixes_file] + runtime_jars).merge(ctx.attr._validator[DefaultInfo].default_runfiles)

    return [DefaultInfo(executable = exec, runfiles = runfiles)]

classpath_collision_test = rule(
    implementation = _impl,
    attrs = {
        "target": attr.label(providers = [JavaInfo]),
        "ignore_prefixes": attr.string_list(doc = "prefixes of jar entries to ignore", default = []),
        "ignore_suffixes": attr.string_list(doc = "suffixes of jar entries to ignore", default = []),
        "_validator": attr.label(providers = [DefaultInfo], default = "//src/main/com/bazelbuild/java/classpath:classpath_run"),
    },
    test = True,
)
