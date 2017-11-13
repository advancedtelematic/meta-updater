import unittest
import os
import logging

from oeqa.selftest.base import oeSelfTest
from oeqa.utils.commands import runCmd, bitbake, get_bb_var, get_bb_vars
import subprocess
from oeqa.selftest.qemucommand import QemuCommand
import time

class UpdaterTests(oeSelfTest):

    @classmethod
    def setUpClass(cls):
        logger = logging.getLogger("selftest")
        logger.info('Running bitbake to build aktualizr-native tools and garage-sign-native')
        bitbake('aktualizr-native garage-sign-native')

    def test_help(self):
        bb_vars = get_bb_vars(['SYSROOT_DESTDIR', 'bindir'], 'aktualizr-native')
        p = bb_vars['SYSROOT_DESTDIR'] + bb_vars['bindir'] + "/" + "garage-push"
        self.assertTrue(os.path.isfile(p), msg = "No garage-push found (%s)" % p)
        result = runCmd('%s --help' % p, ignore_status=True)
        self.assertEqual(result.status, 0, "Status not equal to 0. output: %s" % result.output)

    def test_java(self):
        result = runCmd('which java', ignore_status=True)
        self.assertEqual(result.status, 0, "Java not found.")

    def test_sign(self):
        bb_vars = get_bb_vars(['SYSROOT_DESTDIR', 'bindir'], 'garage-sign-native')
        p = bb_vars['SYSROOT_DESTDIR'] + bb_vars['bindir'] + "/" + "garage-sign"
        self.assertTrue(os.path.isfile(p), msg = "No garage-sign found (%s)" % p)
        result = runCmd('%s --help' % p, ignore_status=True)
        self.assertEqual(result.status, 0, "Status not equal to 0. output: %s" % result.output)

    def test_push(self):
        bitbake('core-image-minimal')
        self.write_config('IMAGE_INSTALL_append = " man "')
        bitbake('core-image-minimal')

    def test_hsm(self):
        self.write_config('SOTA_CLIENT_FEATURES="hsm hsm-test"')
        bitbake('core-image-minimal')

    def test_qemu(self):
        print('')
        # Create empty object.
        args = type('', (), {})()
        args.imagename = 'core-image-minimal'
        args.mac = None
        args.dir = 'tmp/deploy/images'
        args.efi = False
        args.machine = None
        args.no_kvm = False
        args.no_gui = True
        args.gdb = False
        args.pcap = None
        args.overlay = None
        args.dry_run = False

        qemu_command = QemuCommand(args)
        cmdline = qemu_command.command_line()
        print('Booting image with run-qemu-ota...')
        s = subprocess.Popen(cmdline)
        time.sleep(10)
        print('Machine name (hostname) of device is:')
        ssh_cmd = ['ssh', '-q', '-o', 'UserKnownHostsFile=/dev/null', '-o', 'StrictHostKeyChecking=no', 'root@localhost', '-p', str(qemu_command.ssh_port), 'hostname']
        s2 = subprocess.Popen(ssh_cmd)
        time.sleep(5)
        try:
            s.terminate()
        except KeyboardInterrupt:
            pass
