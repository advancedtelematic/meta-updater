#!/usr/bin/env python3

from argparse import ArgumentParser
import os.path
import sys

scripts_path = os.path.dirname(os.path.realpath(__file__))
bb_lib_path = os.path.abspath(scripts_path + '/../../poky/bitbake/lib')
sys.path = sys.path + [bb_lib_path]

import bb.fetch2
import bb.tinfoil


PRINT_PROGRESS = True
SKIP_BUILD_TOOLS = True
KNOWN_BUILD_TOOLS = ['virtual/x86_64-poky-linux-gcc', # gcc-cross-x86_64
                     'virtual/x86_64-poky-linux-compilerlibs', # gcc-runtime
                     'virtual/i586-poky-linux-gcc', # gcc-cross-i586
                     'virtual/i586-poky-linux-compilerlibs', # gcc-runtime
                     'virtual/libc', # glibc
                     'virtual/libintl', # glibc
                     'virtual/libiconv', # glibc
                     'virtual/crypt', # glibc
                     'autoconf-native',
                     'automake-native',
                     'libtool-native',
                     'gnu-config-native',
                     'm4-native',
                     'texinfo-dummy-native',
                     'gettext-minimal-native',
                     'libtool-cross',
                     'gettext-native',
                     'util-linux-native',
                     'pkgconfig-native',
                     'makedepend-native']


def get_recipe_info(tinfoil, rn):
    try:
        info = tinfoil.get_recipe_info(rn)
    except Exception:
        print('Failed to get recipe info for: %s' % rn)
        return []
    if not info:
        print('No recipe info found for: %s' % rn)
        return []
    append_files = tinfoil.get_file_appends(info.fn)
    appends = True
    data = tinfoil.parse_recipe_file(info.fn, appends, append_files)
    data.pn = info.pn
    data.pv = info.pv
    return data


def print_package(manifest_file, data, is_project):
    src_uri = data.getVar('SRC_URI').split()
    lic = data.getVar('LICENSE')
    summary = data.getVar('SUMMARY')
    homepage = data.getVar('HOMEPAGE')
    srcrev = data.getVar('SRCREV')
    branch = data.getVar('BRANCH')

    if is_project:
        manifest_file.write('  id:\n')
    else:
        manifest_file.write('- id:\n')
    manifest_file.write('    package_manager: "Yocto"\n')
    manifest_file.write('    namespace: ""\n')
    manifest_file.write('    name: "%s"\n' % data.pn)
    manifest_file.write('    version: "%s"\n' % data.pv)
    manifest_file.write('  declared_lics:\n')
    manifest_file.write('  - "%s"\n' % lic)
    if is_project:
        manifest_file.write('  aliases: []\n')
    if summary:
        manifest_file.write('  description: "%s"\n' % summary)
    else:
        description = data.getVar('DESCRIPTION')
        manifest_file.write('  description: "%s"\n' % description)
    manifest_file.write('  homepage_url: "%s"\n' % homepage)
    # Binary artifacts almost never exist in Yocto.
    manifest_file.write('  binary_artifact:\n')
    manifest_file.write('    url: ""\n')
    manifest_file.write('    hash: ""\n')
    manifest_file.write('    hash_algorithm: ""\n')
    manifest_file.write('  source_artifact:\n')
    repos = []
    for src in src_uri:
        # Strip options.
        # TODO: ignore files with apply=false?
        src = src.split(';', maxsplit=1)[0]
        src_type = src.split('://', maxsplit=1)[0]
        if src_type == 'file':
            # TODO: Get full path of patches and other files within the source
            # repo, not just the filesystem?
            fetch = bb.fetch2.Fetch([], data)
            local = fetch.localpath(src)
            manifest_file.write('  - "%s"\n' % local)
        else:
            manifest_file.write('  - "%s"\n' % src)
            if src_type != 'http' and src_type != 'https' and src_type != 'ftp' and src_type != 'ssh':
                repos.append(src)
    if len(repos) > 1:
        print('Multiple repos for one package are not supported. Package: %s' % info.pn)
    for repo in repos:
        vcs_type, url = repo.split('://', maxsplit=1)
        manifest_file.write('  vcs:\n')
        if vcs_type == 'gitsm':
            vcs_type = 'git'
        manifest_file.write('    type: "%s"\n' % vcs_type)
        manifest_file.write('    url: "%s"\n' % url)
        # TODO: Actually support multiple repos here:
        # TODO: catch and replace AUTOINC?
        manifest_file.write('    revision: "%s"\n' % srcrev)
        manifest_file.write('    branch: "%s"\n' % branch)


def find_dependencies(manifest_file, tinfoil, assume_provided, recipe_info, packages, rn, order):
    data = recipe_info[rn]
    # Filter out packages from the assume_provided list.
    depends = []
    for dep in data.depends:
        if dep not in assume_provided:
            depends.append(dep)

    if PRINT_PROGRESS:
        # Print high-order dependencies as a form of logging/progress notifcation.
        if order == 2:
            print(rn)
        if order == 3:
            print('  ' + rn)

    # order == 1 is for the initial recipe. We've already printed its
    # information, so skip it.
    if order > 1:
        spaces = '  ' * order
        manifest_file.write('%s- namespace: ""\n' % spaces)
        manifest_file.write('%s  name: "%s"\n' % (spaces, data.pn))
        manifest_file.write('%s  version: "%s"\n' % (spaces, data.pv))
        if not depends:
            manifest_file.write('%s  dependencies: []\n' % spaces)
        else:
            manifest_file.write('%s  dependencies:\n' % spaces)

    # First find all dependencies not seen yet to our master list.
    for dep in depends:
        if dep not in packages:
            packages.append(dep)
            dep_data = get_recipe_info(tinfoil, dep)
            # Do this once now to reduce the number of bitbake calls.
            dep_data.depends = dep_data.getVar('DEPENDS').split()
            recipe_info[dep] = dep_data

    # Then recursively analyze all of the dependencies for the current recipe.
    for dep in depends:
        find_dependencies(manifest_file, tinfoil, assume_provided, recipe_info, packages, dep, order + 1)

    if order > 1:
        manifest_file.write('%s  errors: []\n' % spaces)


def main():
    parser = ArgumentParser(description='Find all dependencies of a recipe.')
    parser.add_argument('recipe', metavar='recipe', help='a recipe to investigate')
    args = parser.parse_args()
    rn = args.recipe
    with bb.tinfoil.Tinfoil() as tinfoil:
        tinfoil.prepare()
        # These are the packages that bitbake assumes are provided by the host
        # system. They do not have recipes, so searching tinfoil for them will
        # not work. Anyway, by nature they are only build tools and will not be
        # distributed in an image.
        assume_provided = tinfoil.config_data.getVar('ASSUME_PROVIDED').split()
        if SKIP_BUILD_TOOLS:
            assume_provided.extend(KNOWN_BUILD_TOOLS)

        data = get_recipe_info(tinfoil, rn)
        if not data:
            print('Nothing to do!')
            return

        with open(rn + '-dependencies.yml', "w") as manifest_file:
            manifest_file.write('project:\n')
            data.depends = []
            depends = data.getVar('DEPENDS').split()
            for dep in depends:
                if dep not in assume_provided:
                    data.depends.append(dep)
            print_package(manifest_file, data, is_project=True)
            manifest_file.write('  scopes:\n')
            manifest_file.write('  - name: "all"\n')
            manifest_file.write('    delivered: true\n')
            if not data.depends:
                manifest_file.write('    dependencies: []\n')
            else:
                manifest_file.write('    dependencies:\n')

            recipe_info = dict([(rn, data)])
            packages = []
            find_dependencies(manifest_file, tinfoil, assume_provided, recipe_info, packages, rn, order=1)

            manifest_file.write('packages:\n')

            # Iterate through the list of packages found to print out their full
            # information. Skip the initial recipe since we already printed it out.
            for p in packages:
                if p is not rn:
                    data = recipe_info[p]
                    print_package(manifest_file, data, is_project=False)


if __name__ == "__main__":
    main()
