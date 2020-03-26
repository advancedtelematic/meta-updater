import os
import oe.path
import logging
import re
import subprocess
from time import sleep

from oeqa.utils.commands import runCmd, bitbake, get_bb_var, get_bb_vars
from qemucommand import QemuCommand

logger = logging.getLogger("selftest")


def qemu_launch(efi=False, machine=None, imagename='core-image-minimal', **kwargs):
    qemu_bake_image(imagename)
    return qemu_boot_image(efi=efi, machine=machine, imagename=imagename, **kwargs)


def qemu_terminate(s):
    try:
        s.terminate()
        s.wait(timeout=10)
    except KeyboardInterrupt:
        pass


def qemu_boot_image(imagename, **kwargs):
    # Create empty object.
    args = type('', (), {})()
    args.imagename = imagename
    args.mac = kwargs.get('mac', None)
    # Could use DEPLOY_DIR_IMAGE here but it's already in the machine
    # subdirectory.
    args.dir = 'tmp/deploy/images'
    args.efi = kwargs.get('efi', False)
    args.bootloader = kwargs.get('bootloader', None)
    args.machine = kwargs.get('machine', None)
    args.mem = kwargs.get('mem', '128M')
    qemu_use_kvm = get_bb_var("QEMU_USE_KVM")
    if qemu_use_kvm and \
            (qemu_use_kvm == 'True' and 'x86' in args.machine or
             get_bb_var('MACHINE') in qemu_use_kvm.split()):
        args.kvm = True
    else:
        args.kvm = None  # Autodetect
    args.no_gui = kwargs.get('no_gui', True)
    args.gdb = kwargs.get('gdb', False)
    args.pcap = kwargs.get('pcap', None)
    args.overlay = kwargs.get('overlay', None)
    args.dry_run = kwargs.get('dry_run', False)
    args.secondary_network = kwargs.get('secondary_network', False)
    args.uboot_enable = kwargs.get('uboot_enable', 'yes')

    qemu = QemuCommand(args)
    cmdline = qemu.command_line()
    print('Booting image with run-qemu-ota...')
    s = subprocess.Popen(cmdline)
    sleep(kwargs.get('wait_for_boot_time', 10))
    return qemu, s


def qemu_bake_image(imagename):
    logger.info('Running bitbake to build {}'.format(imagename))
    bitbake(imagename)


def qemu_send_command(port, command, timeout=120):
    command = ['ssh -q -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@localhost -p ' +
               str(port) + ' "' + command + '"']
    s2 = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    stdout, stderr = s2.communicate(timeout=timeout)
    return stdout, stderr, s2.returncode


def metadir():
    # Assume the directory layout for finding other layers. We could also
    # make assumptions by using 'show-layers', but either way, if the
    # layers we need aren't where we expect them, we are out of luck.
    path = os.path.abspath(os.path.dirname(__file__))
    metadir = path + "/../../../../../"

    return metadir


def akt_native_run(testInst, cmd, **kwargs):
    # run a command supplied by aktualizr-native and checks that:
    # - the executable exists
    # - the command runs without error
    #
    # Requirements in base test class (setUpClass for example):
    #   bitbake aktualizr-native
    #   bitbake build-sysroots -c build_native_sysroot
    #
    # (technique found in poky/meta/lib/oeqa/selftest/cases/package.py)
    bb_vars = get_bb_vars(['STAGING_DIR', 'BUILD_ARCH'])
    sysroot = oe.path.join(bb_vars['STAGING_DIR'], bb_vars['BUILD_ARCH'])

    result = runCmd(cmd, native_sysroot=sysroot, ignore_status=True, **kwargs)
    testInst.assertEqual(result.status, 0, "Status not equal to 0. output: %s" % result.output)


def verifyNotProvisioned(testInst, machine):
    print('Checking output of aktualizr-info:')
    ran_ok = False
    for delay in [5, 5, 5, 5, 10, 10, 10, 10]:
        stdout, stderr, retcode = testInst.qemu_command('aktualizr-info')
        if retcode == 0 and stderr == b'':
            ran_ok = True
            break
        sleep(delay)
    testInst.assertTrue(ran_ok, 'aktualizr-info failed: ' + stderr.decode() + stdout.decode())

    # Verify that device has NOT yet provisioned.
    testInst.assertIn(b'Couldn\'t load device ID', stdout,
                      'Device already provisioned!? ' + stderr.decode() + stdout.decode())
    testInst.assertIn(b'Couldn\'t load ECU serials', stdout,
                      'Device already provisioned!? ' + stderr.decode() + stdout.decode())
    testInst.assertIn(b'Provisioned on server: no', stdout,
                      'Device already provisioned!? ' + stderr.decode() + stdout.decode())
    testInst.assertIn(b'Fetched metadata: no', stdout,
                      'Device already provisioned!? ' + stderr.decode() + stdout.decode())


def verifyProvisioned(testInst, machine, hwid=''):
    # Verify that device HAS provisioned.
    # First loop while waiting for the device to boot.
    ran_ok = False
    for delay in [5, 5, 5, 5, 10, 10, 10, 10]:
        stdout, stderr, retcode = testInst.qemu_command('aktualizr-info')
        if retcode == 0 and stderr == b'':
            ran_ok = True
            break
        sleep(delay)
    testInst.assertTrue(ran_ok, 'aktualizr-info failed: ' + stderr.decode() + stdout.decode())
    # Then wait for aktualizr to provision.
    if stdout.decode().find('Fetched metadata: yes') < 0:
        stdout, stderr, retcode = testInst.qemu_command('aktualizr-info --wait-until-provisioned')
        testInst.assertFalse(retcode, 'aktualizr-info failed: ' + stderr.decode() + stdout.decode())
        testInst.assertEqual(stderr, b'', 'aktualizr-info failed: ' + stderr.decode() + stdout.decode())
    testInst.assertIn(b'Device ID: ', stdout, 'Provisioning failed: ' + stderr.decode() + stdout.decode())
    if hwid == '':
        testInst.assertIn(b'Primary ECU hardware ID: ' + machine.encode(), stdout,
                  'Provisioning failed: ' + stderr.decode() + stdout.decode())
    else:
        testInst.assertIn(b'Primary ECU hardware ID: ' + hwid.encode(), stdout,
                  'Provisioning failed: ' + stderr.decode() + stdout.decode())
    testInst.assertIn(b'Fetched metadata: yes', stdout, 'Provisioning failed: ' + stderr.decode() + stdout.decode())
    p = re.compile(r'Device ID: ([a-z0-9-]*)\n')
    m = p.search(stdout.decode())
    testInst.assertTrue(m, 'Device ID could not be read: ' + stderr.decode() + stdout.decode())
    testInst.assertGreater(m.lastindex, 0, 'Device ID could not be read: ' + stderr.decode() + stdout.decode())
    logger.info('Device successfully provisioned with ID: ' + m.group(1))

# vim:set ts=4 sw=4 sts=4 expandtab:
