#!/usr/bin/env python3

import os.path
import sys

scripts_path = os.path.dirname(os.path.realpath(__file__))
lib_path = os.path.abspath(scripts_path + '/../../poky/bitbake/lib')
sys.path = sys.path + [lib_path]

import bb.tinfoil


def print_deps(tinfoil, abcd_file, rn):
    try:
        info = tinfoil.get_recipe_info(rn)
    except:
        # fails on hostperl-runtime-native, virtual/libintl-native, grep-native, virtual/libiconv-native
        print('Failed to get recipe info for: %s' % rn)
        return []
    if not info:
        # fails on the above and virtual/crypt-native
        print('No recipe info found for: %s' % rn)
        return []
    append_files = tinfoil.get_file_appends(info.fn)
    appends = True
    data = tinfoil.parse_recipe_file(info.fn, appends, append_files)
    src_uri = data.getVar('SRC_URI').split()
    lic = data.getVar('LICENSE')
    summary = data.getVar('SUMMARY')
    description = data.getVar('DESCRIPTION')
    homepage = data.getVar('HOMEPAGE')
    srcrev = data.getVar('SRCREV')
    branch = data.getVar('BRANCH')
    depends = data.getVar('DEPENDS').split()

    abcd_file.write('- id:\n')
    abcd_file.write('    package_manager: "Yocto"\n')
    abcd_file.write('    name: "%s"\n' % info.pn)
    abcd_file.write('    version: "%s"\n' % info.pv)
    abcd_file.write('  declared_lics:\n')
    abcd_file.write('  - "%s"\n' % lic)
    if summary:
        abcd_file.write('  description: "%s"\n' % summary)
    else:
        abcd_file.write('  description: "%s"\n' % description)
    abcd_file.write('  homepage_url: "%s"\n' % homepage)
    abcd_file.write('  source_artifact:\n')
    for src in src_uri:
        # TODO: Get full path of patches?
        abcd_file.write('  - "%s"\n' % src)
    # TODO: Check more than the first and not just git
    if src_uri and 'git' in src_uri[0]:
        abcd_file.write('  vcs:\n')
        abcd_file.write('    type: "git"\n')
        abcd_file.write('    url: "%s"\n' % src_uri[0])
        abcd_file.write('    revision: "%s"\n' % srcrev)
        abcd_file.write('    branch: "%s"\n' % branch)

    abcd_file.write('  dependencies:\n')
    for dep in depends:
        abcd_file.write('  - "%s"\n' % dep)
        # TODO: continue nesting here?

    return depends


def main():
    abcd_manifest = 'manifest.abcd'
    with open(abcd_manifest, "w") as abcd_file, bb.tinfoil.Tinfoil() as tinfoil:
        tinfoil.prepare()
        abcd_file.write('packages:\n')

        recipes_to_check = ['aktualizr',
                            'aktualizr-native',
                            'aktualizr-auto-prov',
                            'aktualizr-implicit-prov',
                            'aktualizr-ca-implicit-prov',
                            'aktualizr-hsm-prov',
                            'aktualizr-disable-send-ip',
                            'aktualizr-example-interface',
                            'aktualizr-log-debug']

        for recipe in recipes_to_check:
            depends = print_deps(tinfoil, abcd_file, recipe)
            for dep in depends:
                if dep not in recipes_to_check:
                    recipes_to_check.append(dep)


if __name__ == "__main__":
    main()
