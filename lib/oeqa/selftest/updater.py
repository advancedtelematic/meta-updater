import unittest
import os
import logging
import subprocess
import time

from oeqa.selftest.base import oeSelfTest
from oeqa.utils.commands import runCmd, bitbake, get_bb_var, get_bb_vars
from oeqa.selftest.qemucommand import QemuCommand


class SotaToolsTests(oeSelfTest):

    @classmethod
    def setUpClass(cls):
        logger = logging.getLogger("selftest")
        logger.info('Running bitbake to build aktualizr-native tools')
        bitbake('aktualizr-native')

    def test_push_help(self):
        bb_vars = get_bb_vars(['SYSROOT_DESTDIR', 'bindir'], 'aktualizr-native')
        p = bb_vars['SYSROOT_DESTDIR'] + bb_vars['bindir'] + "/" + "garage-push"
        self.assertTrue(os.path.isfile(p), msg = "No garage-push found (%s)" % p)
        result = runCmd('%s --help' % p, ignore_status=True)
        self.assertEqual(result.status, 0, "Status not equal to 0. output: %s" % result.output)

    def test_deploy_help(self):
        bb_vars = get_bb_vars(['SYSROOT_DESTDIR', 'bindir'], 'aktualizr-native')
        p = bb_vars['SYSROOT_DESTDIR'] + bb_vars['bindir'] + "/" + "garage-deploy"
        self.assertTrue(os.path.isfile(p), msg = "No garage-deploy found (%s)" % p)
        result = runCmd('%s --help' % p, ignore_status=True)
        self.assertEqual(result.status, 0, "Status not equal to 0. output: %s" % result.output)


class GarageSignTests(oeSelfTest):

    @classmethod
    def setUpClass(cls):
        logger = logging.getLogger("selftest")
        logger.info('Running bitbake to build garage-sign-native')
        bitbake('garage-sign-native')

    def test_help(self):
        bb_vars = get_bb_vars(['SYSROOT_DESTDIR', 'bindir'], 'garage-sign-native')
        p = bb_vars['SYSROOT_DESTDIR'] + bb_vars['bindir'] + "/" + "garage-sign"
        self.assertTrue(os.path.isfile(p), msg = "No garage-sign found (%s)" % p)
        result = runCmd('%s --help' % p, ignore_status=True)
        self.assertEqual(result.status, 0, "Status not equal to 0. output: %s" % result.output)


class HsmTests(oeSelfTest):

    def test_hsm(self):
        self.write_config('SOTA_CLIENT_FEATURES="hsm hsm-test"')
        bitbake('core-image-minimal')


class GeneralTests(oeSelfTest):

    def test_feature_sota(self):
        result = get_bb_var('DISTRO_FEATURES').find('sota')
        self.assertNotEqual(result, -1, 'Feature "sota" not set at DISTRO_FEATURES');

    def test_feature_systemd(self):
        result = get_bb_var('DISTRO_FEATURES').find('systemd')
        self.assertNotEqual(result, -1, 'Feature "systemd" not set at DISTRO_FEATURES');

    def test_credentials(self):
        bitbake('core-image-minimal')
        credentials = get_bb_var('SOTA_PACKED_CREDENTIALS')
        # skip the test if the variable SOTA_PACKED_CREDENTIALS is not set
        if credentials is None:
            raise unittest.SkipTest("Variable 'SOTA_PACKED_CREDENTIALS' not set.")
        # Check if the file exists
        self.assertTrue(os.path.isfile(credentials), "File %s does not exist" % credentials)
        deploydir = get_bb_var('DEPLOY_DIR_IMAGE')
        imagename = get_bb_var('IMAGE_LINK_NAME', 'core-image-minimal')
        # Check if the credentials are included in the output image
        result = runCmd('tar -jtvf %s/%s.tar.bz2 | grep sota_provisioning_credentials.zip' % (deploydir, imagename), ignore_status=True)
        self.assertEqual(result.status, 0, "Status not equal to 0. output: %s" % result.output)

    def test_java(self):
        result = runCmd('which java', ignore_status=True)
        self.assertEqual(result.status, 0, "Java not found.")

    def test_add_package(self):
        print('')
        deploydir = get_bb_var('DEPLOY_DIR_IMAGE')
        imagename = get_bb_var('IMAGE_LINK_NAME', 'core-image-minimal')
        image_path = deploydir + '/' + imagename + '.otaimg'
        logger = logging.getLogger("selftest")

        logger.info('Running bitbake with man in the image package list')
        self.write_config('IMAGE_INSTALL_append = " man "')
        bitbake('-c cleanall man')
        bitbake('core-image-minimal')
        result = runCmd('oe-pkgdata-util find-path /usr/bin/man')
        self.assertEqual(result.output, 'man: /usr/bin/man')
        path1 = os.path.realpath(image_path)
        size1 = os.path.getsize(path1)
        logger.info('First image %s has size %i' % (path1, size1))

        logger.info('Running bitbake without man in the image package list')
        self.write_config('IMAGE_INSTALL_remove = " man "')
        bitbake('-c cleanall man')
        bitbake('core-image-minimal')
        result = runCmd('oe-pkgdata-util find-path /usr/bin/man', ignore_status=True)
        self.assertEqual(result.status, 1, "Status different than 1. output: %s" % result.output)
        self.assertEqual(result.output, 'ERROR: Unable to find any package producing path /usr/bin/man')
        path2 = os.path.realpath(image_path)
        size2 = os.path.getsize(path2)
        logger.info('Second image %s has size %i' % (path2, size2))
        self.assertNotEqual(path1, path2, "Image paths are identical; image was not rebuilt.")
        self.assertNotEqual(size1, size2, "Image sizes are identical; image was not rebuilt.")

    def test_qemu(self):
        print('')
        # Create empty object.
        args = type('', (), {})()
        args.imagename = 'core-image-minimal'
        args.mac = None
        # Could use DEPLOY_DIR_IMAGE her but it's already in the machine
        # subdirectory.
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

