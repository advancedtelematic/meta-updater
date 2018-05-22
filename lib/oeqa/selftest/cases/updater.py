# pylint: disable=C0111,C0325
import os
import logging
import re
import subprocess
import unittest
from time import sleep

from oeqa.selftest.case import OESelftestTestCase
from oeqa.utils.commands import runCmd, bitbake, get_bb_var, get_bb_vars
from qemucommand import QemuCommand


class SotaToolsTests(OESelftestTestCase):

    @classmethod
    def setUpClass(cls):
        super(SotaToolsTests, cls).setUpClass()
        logger = logging.getLogger("selftest")
        logger.info('Running bitbake to build aktualizr-native tools')
        bitbake('aktualizr-native')

    def test_push_help(self):
        akt_native_run(self, 'garage-push --help')

    def test_deploy_help(self):
        akt_native_run(self, 'garage-deploy --help')

    def test_garagesign_help(self):
        akt_native_run(self, 'garage-sign --help')


class GeneralTests(OESelftestTestCase):

    def test_feature_sota(self):
        result = get_bb_var('DISTRO_FEATURES').find('sota')
        self.assertNotEqual(result, -1, 'Feature "sota" not set at DISTRO_FEATURES')

    def test_feature_systemd(self):
        result = get_bb_var('DISTRO_FEATURES').find('systemd')
        self.assertNotEqual(result, -1, 'Feature "systemd" not set at DISTRO_FEATURES')

    def test_credentials(self):
        logger = logging.getLogger("selftest")
        logger.info('Running bitbake to build core-image-minimal')
        self.append_config('SOTA_CLIENT_PROV = "aktualizr-auto-prov"')
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
        result = runCmd('tar -jtvf %s/%s.tar.bz2 | grep sota_provisioning_credentials.zip' %
                        (deploydir, imagename), ignore_status=True)
        self.assertEqual(result.status, 0, "Status not equal to 0. output: %s" % result.output)

    def test_java(self):
        result = runCmd('which java', ignore_status=True)
        self.assertEqual(result.status, 0,
                         "Java not found. Do you have a JDK installed on your host machine?")

    def test_add_package(self):
        deploydir = get_bb_var('DEPLOY_DIR_IMAGE')
        imagename = get_bb_var('IMAGE_LINK_NAME', 'core-image-minimal')
        image_path = deploydir + '/' + imagename + '.otaimg'
        logger = logging.getLogger("selftest")

        logger.info('Running bitbake with man in the image package list')
        self.append_config('IMAGE_INSTALL_append = " man "')
        bitbake('-c cleanall man')
        bitbake('core-image-minimal')
        result = runCmd('oe-pkgdata-util find-path /usr/bin/man')
        self.assertEqual(result.output, 'man: /usr/bin/man')
        path1 = os.path.realpath(image_path)
        size1 = os.path.getsize(path1)
        logger.info('First image %s has size %i' % (path1, size1))

        logger.info('Running bitbake without man in the image package list')
        self.append_config('IMAGE_INSTALL_remove = " man "')
        bitbake('-c cleanall man')
        bitbake('core-image-minimal')
        result = runCmd('oe-pkgdata-util find-path /usr/bin/man', ignore_status=True)
        self.assertEqual(result.status, 1, "Status different than 1. output: %s" % result.output)
        self.assertEqual(result.output, 'ERROR: Unable to find any package producing path /usr/bin/man')
        path2 = os.path.realpath(image_path)
        size2 = os.path.getsize(path2)
        logger.info('Second image %s has size %i', path2, size2)
        self.assertNotEqual(path1, path2, "Image paths are identical; image was not rebuilt.")
        self.assertNotEqual(size1, size2, "Image sizes are identical; image was not rebuilt.")


class AktualizrToolsTests(OESelftestTestCase):

    @classmethod
    def setUpClass(cls):
        super(AktualizrToolsTests, cls).setUpClass()
        logger = logging.getLogger("selftest")
        logger.info('Running bitbake to build aktualizr-native tools')
        bitbake('aktualizr-native')

    def test_implicit_writer_help(self):
        akt_native_run(self, 'aktualizr_implicit_writer --help')

    def test_cert_provider_help(self):
        akt_native_run(self, 'aktualizr_cert_provider --help')

    def test_cert_provider_local_output(self):
        logger = logging.getLogger("selftest")
        logger.info('Running bitbake to build aktualizr-implicit-prov')
        bitbake('aktualizr-implicit-prov')
        bb_vars = get_bb_vars(['SOTA_PACKED_CREDENTIALS', 'T'], 'aktualizr-native')
        creds = bb_vars['SOTA_PACKED_CREDENTIALS']
        temp_dir = bb_vars['T']
        bb_vars_prov = get_bb_vars(['STAGING_DIR_NATIVE', 'libdir'], 'aktualizr-implicit-prov')
        config = bb_vars_prov['STAGING_DIR_NATIVE'] + bb_vars_prov['libdir'] + '/sota/sota_implicit_prov.toml'

        akt_native_run(self, 'aktualizr_cert_provider -c {creds} -r -l {temp} -g {config}'
                       .format(creds=creds, temp=temp_dir, config=config))

        # Might be nice if these names weren't hardcoded.
        cert_path = temp_dir + '/client.pem'
        self.assertTrue(os.path.isfile(cert_path), "Client certificate not found at %s." % cert_path)
        self.assertTrue(os.path.getsize(cert_path) > 0, "Client certificate at %s is empty." % cert_path)
        pkey_path = temp_dir + '/pkey.pem'
        self.assertTrue(os.path.isfile(pkey_path), "Private key not found at %s." % pkey_path)
        self.assertTrue(os.path.getsize(pkey_path) > 0, "Private key at %s is empty." % pkey_path)
        ca_path = temp_dir + '/root.crt'
        self.assertTrue(os.path.isfile(ca_path), "Client certificate not found at %s." % ca_path)
        self.assertTrue(os.path.getsize(ca_path) > 0, "Client certificate at %s is empty." % ca_path)


class AutoProvTests(OESelftestTestCase):

    def setUpLocal(self):
        layer = "meta-updater-qemux86-64"
        result = runCmd('bitbake-layers show-layers')
        if re.search(layer, result.output) is None:
            # Assume the directory layout for finding other layers. We could also
            # make assumptions by using 'show-layers', but either way, if the
            # layers we need aren't where we expect them, we are out of like.
            path = os.path.abspath(os.path.dirname(__file__))
            metadir = path + "/../../../../../"
            self.meta_qemu = metadir + layer
            runCmd('bitbake-layers add-layer "%s"' % self.meta_qemu)
        else:
            self.meta_qemu = None
        self.append_config('MACHINE = "qemux86-64"')
        self.append_config('SOTA_CLIENT_PROV = " aktualizr-auto-prov "')
        # Test aktualizr-example-interface package.
        self.append_config('IMAGE_INSTALL_append = " aktualizr-examples aktualizr-example-interface "')
        self.qemu, self.s = qemu_launch(machine='qemux86-64')

    def tearDownLocal(self):
        qemu_terminate(self.s)
        if self.meta_qemu:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_qemu, ignore_status=True)

    def qemu_command(self, command):
        return qemu_send_command(self.qemu.ssh_port, command)

    def test_provisioning(self):
        print('Checking machine name (hostname) of device:')
        stdout, stderr, retcode = self.qemu_command('hostname')
        self.assertEqual(retcode, 0, "Unable to check hostname. " +
                         "Is an ssh daemon (such as dropbear or openssh) installed on the device?")
        machine = get_bb_var('MACHINE', 'core-image-minimal')
        self.assertEqual(stderr, b'', 'Error: ' + stderr.decode())
        # Strip off line ending.
        value = stdout.decode()[:-1]
        self.assertEqual(value, machine,
                         'MACHINE does not match hostname: ' + machine + ', ' + value)
        print(value)
        print('Checking output of aktualizr-info:')
        ran_ok = False
        for delay in [1, 2, 5, 10, 15]:
            stdout, stderr, retcode = self.qemu_command('aktualizr-info')
            if retcode == 0 and stderr == b'':
                ran_ok = True
                break
            sleep(delay)
        self.assertTrue(ran_ok, 'aktualizr-info failed: ' + stderr.decode() + stdout.decode())

        verifyProvisioned(self, machine)
        # Test aktualizr-example-interface package.
        stdout, stderr, retcode = self.qemu_command('aktualizr-info')
        self.assertIn(b'hardware ID: example1', stdout,
                      'Legacy secondary initialization failed: ' + stderr.decode() + stdout.decode())
        self.assertIn(b'hardware ID: example2', stdout,
                      'Legacy secondary initialization failed: ' + stderr.decode() + stdout.decode())


class RpiTests(OESelftestTestCase):

    def setUpLocal(self):
        # Add layers before changing the machine type, otherwise the sanity
        # checker complains loudly.
        layer_python = "meta-openembedded/meta-python"
        layer_rpi = "meta-raspberrypi"
        layer_upd_rpi = "meta-updater-raspberrypi"
        result = runCmd('bitbake-layers show-layers')
        # Assume the directory layout for finding other layers. We could also
        # make assumptions by using 'show-layers', but either way, if the
        # layers we need aren't where we expect them, we are out of like.
        path = os.path.abspath(os.path.dirname(__file__))
        metadir = path + "/../../../../../"
        if re.search(layer_python, result.output) is None:
            self.meta_python = metadir + layer_python
            runCmd('bitbake-layers add-layer "%s"' % self.meta_python)
        else:
            self.meta_python = None
        if re.search(layer_rpi, result.output) is None:
            self.meta_rpi = metadir + layer_rpi
            runCmd('bitbake-layers add-layer "%s"' % self.meta_rpi)
        else:
            self.meta_rpi = None
        if re.search(layer_upd_rpi, result.output) is None:
            self.meta_upd_rpi = metadir + layer_upd_rpi
            runCmd('bitbake-layers add-layer "%s"' % self.meta_upd_rpi)
        else:
            self.meta_upd_rpi = None

        # This is trickier that I would've thought. The fundamental problem is
        # that the qemu layer changes the u-boot file extension to .rom, but
        # raspberrypi still expects .bin. To prevent this, the qemu layer must
        # be temporarily removed if it is present. It has to be removed by name
        # without the complete path, but to add it back when we are done, we
        # need the full path.
        p = re.compile(r'meta-updater-qemux86-64\s*(\S*meta-updater-qemux86-64)\s')
        m = p.search(result.output)
        if m and m.lastindex > 0:
            self.meta_qemu = m.group(1)
            runCmd('bitbake-layers remove-layer meta-updater-qemux86-64')
        else:
            self.meta_qemu = None

        self.append_config('MACHINE = "raspberrypi3"')
        self.append_config('SOTA_CLIENT_PROV = " aktualizr-auto-prov "')

    def tearDownLocal(self):
        if self.meta_qemu:
            runCmd('bitbake-layers add-layer "%s"' % self.meta_qemu, ignore_status=True)
        if self.meta_upd_rpi:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_upd_rpi, ignore_status=True)
        if self.meta_rpi:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_rpi, ignore_status=True)
        if self.meta_python:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_python, ignore_status=True)

    def test_rpi(self):
        logger = logging.getLogger("selftest")
        logger.info('Running bitbake to build rpi-basic-image')
        self.append_config('SOTA_CLIENT_PROV = "aktualizr-auto-prov"')
        bitbake('rpi-basic-image')
        credentials = get_bb_var('SOTA_PACKED_CREDENTIALS')
        # Skip the test if the variable SOTA_PACKED_CREDENTIALS is not set.
        if credentials is None:
            raise unittest.SkipTest("Variable 'SOTA_PACKED_CREDENTIALS' not set.")
        # Check if the file exists.
        self.assertTrue(os.path.isfile(credentials), "File %s does not exist" % credentials)
        deploydir = get_bb_var('DEPLOY_DIR_IMAGE')
        imagename = get_bb_var('IMAGE_LINK_NAME', 'rpi-basic-image')
        # Check if the credentials are included in the output image.
        result = runCmd('tar -jtvf %s/%s.tar.bz2 | grep sota_provisioning_credentials.zip' %
                        (deploydir, imagename), ignore_status=True)
        self.assertEqual(result.status, 0, "Status not equal to 0. output: %s" % result.output)


class GrubTests(OESelftestTestCase):

    def setUpLocal(self):
        layer_intel = "meta-intel"
        layer_minnow = "meta-updater-minnowboard"
        result = runCmd('bitbake-layers show-layers')
        # Assume the directory layout for finding other layers. We could also
        # make assumptions by using 'show-layers', but either way, if the
        # layers we need aren't where we expect them, we are out of like.
        path = os.path.abspath(os.path.dirname(__file__))
        metadir = path + "/../../../../../"
        if re.search(layer_intel, result.output) is None:
            self.meta_intel = metadir + layer_intel
            runCmd('bitbake-layers add-layer "%s"' % self.meta_intel)
        else:
            self.meta_intel = None
        if re.search(layer_minnow, result.output) is None:
            self.meta_minnow = metadir + layer_minnow
            runCmd('bitbake-layers add-layer "%s"' % self.meta_minnow)
        else:
            self.meta_minnow = None
        self.append_config('MACHINE = "intel-corei7-64"')
        self.append_config('OSTREE_BOOTLOADER = "grub"')
        self.append_config('SOTA_CLIENT_PROV = " aktualizr-auto-prov "')
        self.qemu, self.s = qemu_launch(efi=True, machine='intel-corei7-64')

    def tearDownLocal(self):
        qemu_terminate(self.s)
        if self.meta_intel:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_intel, ignore_status=True)
        if self.meta_minnow:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_minnow, ignore_status=True)

    def qemu_command(self, command):
        return qemu_send_command(self.qemu.ssh_port, command)

    def test_grub(self):
        print('Checking machine name (hostname) of device:')
        stdout, stderr, retcode = self.qemu_command('hostname')
        self.assertEqual(retcode, 0, "Unable to check hostname. " +
                         "Is an ssh daemon (such as dropbear or openssh) installed on the device?")
        machine = get_bb_var('MACHINE', 'core-image-minimal')
        self.assertEqual(stderr, b'', 'Error: ' + stderr.decode())
        # Strip off line ending.
        value = stdout.decode()[:-1]
        self.assertEqual(value, machine,
                         'MACHINE does not match hostname: ' + machine + ', ' + value +
                         '\nIs TianoCore ovmf installed on your host machine?')
        print(value)
        print('Checking output of aktualizr-info:')
        ran_ok = False
        for delay in [1, 2, 5, 10, 15]:
            stdout, stderr, retcode = self.qemu_command('aktualizr-info')
            if retcode == 0 and stderr == b'':
                ran_ok = True
                break
            sleep(delay)
        self.assertTrue(ran_ok, 'aktualizr-info failed: ' + stderr.decode() + stdout.decode())

        verifyProvisioned(self, machine)


class ImplProvTests(OESelftestTestCase):

    def setUpLocal(self):
        layer = "meta-updater-qemux86-64"
        result = runCmd('bitbake-layers show-layers')
        if re.search(layer, result.output) is None:
            # Assume the directory layout for finding other layers. We could also
            # make assumptions by using 'show-layers', but either way, if the
            # layers we need aren't where we expect them, we are out of like.
            path = os.path.abspath(os.path.dirname(__file__))
            metadir = path + "/../../../../../"
            self.meta_qemu = metadir + layer
            runCmd('bitbake-layers add-layer "%s"' % self.meta_qemu)
        else:
            self.meta_qemu = None
        self.append_config('MACHINE = "qemux86-64"')
        self.append_config('SOTA_CLIENT_PROV = " aktualizr-implicit-prov "')
        self.qemu, self.s = qemu_launch(machine='qemux86-64')

    def tearDownLocal(self):
        qemu_terminate(self.s)
        if self.meta_qemu:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_qemu, ignore_status=True)

    def qemu_command(self, command):
        return qemu_send_command(self.qemu.ssh_port, command)

    def test_provisioning(self):
        print('Checking machine name (hostname) of device:')
        stdout, stderr, retcode = self.qemu_command('hostname')
        self.assertEqual(retcode, 0, "Unable to check hostname. " +
                         "Is an ssh daemon (such as dropbear or openssh) installed on the device?")
        machine = get_bb_var('MACHINE', 'core-image-minimal')
        self.assertEqual(stderr, b'', 'Error: ' + stderr.decode())
        # Strip off line ending.
        value = stdout.decode()[:-1]
        self.assertEqual(value, machine,
                         'MACHINE does not match hostname: ' + machine + ', ' + value)
        print(value)
        print('Checking output of aktualizr-info:')
        ran_ok = False
        for delay in [1, 2, 5, 10, 15]:
            stdout, stderr, retcode = self.qemu_command('aktualizr-info')
            if retcode == 0 and stderr == b'':
                ran_ok = True
                break
            sleep(delay)
        self.assertTrue(ran_ok, 'aktualizr-info failed: ' + stderr.decode() + stdout.decode())
        # Verify that device has NOT yet provisioned.
        self.assertIn(b'Couldn\'t load device ID', stdout,
                      'Device already provisioned!? ' + stderr.decode() + stdout.decode())
        self.assertIn(b'Couldn\'t load ECU serials', stdout,
                      'Device already provisioned!? ' + stderr.decode() + stdout.decode())
        self.assertIn(b'Provisioned on server: no', stdout,
                      'Device already provisioned!? ' + stderr.decode() + stdout.decode())
        self.assertIn(b'Fetched metadata: no', stdout,
                      'Device already provisioned!? ' + stderr.decode() + stdout.decode())

        # Run cert_provider.
        bb_vars = get_bb_vars(['SOTA_PACKED_CREDENTIALS'], 'aktualizr-native')
        creds = bb_vars['SOTA_PACKED_CREDENTIALS']
        bb_vars_prov = get_bb_vars(['STAGING_DIR_NATIVE', 'libdir'], 'aktualizr-implicit-prov')
        config = bb_vars_prov['STAGING_DIR_NATIVE'] + bb_vars_prov['libdir'] + '/sota/sota_implicit_prov.toml'

        akt_native_run(self, 'aktualizr_cert_provider -c {creds} -t root@localhost -p {port} -s -g {config}'
                       .format(creds=creds, port=self.qemu.ssh_port, config=config))

        verifyProvisioned(self, machine)


class HsmTests(OESelftestTestCase):

    def setUpLocal(self):
        layer = "meta-updater-qemux86-64"
        result = runCmd('bitbake-layers show-layers')
        if re.search(layer, result.output) is None:
            # Assume the directory layout for finding other layers. We could also
            # make assumptions by using 'show-layers', but either way, if the
            # layers we need aren't where we expect them, we are out of like.
            path = os.path.abspath(os.path.dirname(__file__))
            metadir = path + "/../../../../../"
            self.meta_qemu = metadir + layer
            runCmd('bitbake-layers add-layer "%s"' % self.meta_qemu)
        else:
            self.meta_qemu = None
        self.append_config('MACHINE = "qemux86-64"')
        self.append_config('SOTA_CLIENT_PROV = "aktualizr-hsm-prov"')
        self.append_config('SOTA_CLIENT_FEATURES = "hsm"')
        self.qemu, self.s = qemu_launch(machine='qemux86-64')

    def tearDownLocal(self):
        qemu_terminate(self.s)
        if self.meta_qemu:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_qemu, ignore_status=True)

    def qemu_command(self, command):
        return qemu_send_command(self.qemu.ssh_port, command)

    def test_provisioning(self):
        print('Checking machine name (hostname) of device:')
        stdout, stderr, retcode = self.qemu_command('hostname')
        self.assertEqual(retcode, 0, "Unable to check hostname. " +
                         "Is an ssh daemon (such as dropbear or openssh) installed on the device?")
        machine = get_bb_var('MACHINE', 'core-image-minimal')
        self.assertEqual(stderr, b'', 'Error: ' + stderr.decode())
        # Strip off line ending.
        value = stdout.decode()[:-1]
        self.assertEqual(value, machine,
                         'MACHINE does not match hostname: ' + machine + ', ' + value +
                         '\nIs tianocore ovmf installed?')
        print(value)
        print('Checking output of aktualizr-info:')
        ran_ok = False
        for delay in [1, 2, 5, 10, 15]:
            stdout, stderr, retcode = self.qemu_command('aktualizr-info')
            if retcode == 0 and stderr == b'':
                ran_ok = True
                break
            sleep(delay)
        self.assertTrue(ran_ok, 'aktualizr-info failed: ' + stderr.decode() + stdout.decode())
        # Verify that device has NOT yet provisioned.
        self.assertIn(b'Couldn\'t load device ID', stdout,
                      'Device already provisioned!? ' + stderr.decode() + stdout.decode())
        self.assertIn(b'Couldn\'t load ECU serials', stdout,
                      'Device already provisioned!? ' + stderr.decode() + stdout.decode())
        self.assertIn(b'Provisioned on server: no', stdout,
                      'Device already provisioned!? ' + stderr.decode() + stdout.decode())
        self.assertIn(b'Fetched metadata: no', stdout,
                      'Device already provisioned!? ' + stderr.decode() + stdout.decode())

        # Verify that HSM is not yet initialized.
        pkcs11_command = 'pkcs11-tool --module=/usr/lib/softhsm/libsofthsm2.so -O'
        stdout, stderr, retcode = self.qemu_command(pkcs11_command)
        self.assertNotEqual(retcode, 0, 'pkcs11-tool succeeded before initialization: ' +
                            stdout.decode() + stderr.decode())
        softhsm2_command = 'softhsm2-util --show-slots'
        stdout, stderr, retcode = self.qemu_command(softhsm2_command)
        self.assertNotEqual(retcode, 0, 'softhsm2-tool succeeded before initialization: ' +
                        stdout.decode() + stderr.decode())

        # Run cert_provider.
        bb_vars = get_bb_vars(['SOTA_PACKED_CREDENTIALS'], 'aktualizr-native')
        creds = bb_vars['SOTA_PACKED_CREDENTIALS']
        bb_vars_prov = get_bb_vars(['STAGING_DIR_NATIVE', 'libdir'], 'aktualizr-hsm-prov')
        config = bb_vars_prov['STAGING_DIR_NATIVE'] + bb_vars_prov['libdir'] + '/sota/sota_hsm_prov.toml'

        akt_native_run(self, 'aktualizr_cert_provider -c {creds} -t root@localhost -p {port} -r -s -g {config}'
                       .format(creds=creds, port=self.qemu.ssh_port, config=config))

        # Verify that HSM is able to initialize.
        ran_ok = False
        for delay in [5, 5, 5, 5, 10]:
            sleep(delay)
            p11_out, p11_err, p11_ret = self.qemu_command(pkcs11_command)
            hsm_out, hsm_err, hsm_ret = self.qemu_command(softhsm2_command)
            if p11_ret == 0 and hsm_ret == 0 and hsm_err == b'':
                ran_ok = True
                break
        self.assertTrue(ran_ok, 'pkcs11-tool or softhsm2-tool failed: ' + p11_err.decode() +
                        p11_out.decode() + hsm_err.decode() + hsm_out.decode())
        self.assertIn(b'present token', p11_err, 'pkcs11-tool failed: ' + p11_err.decode() + p11_out.decode())
        self.assertIn(b'X.509 cert', p11_out, 'pkcs11-tool failed: ' + p11_err.decode() + p11_out.decode())
        self.assertIn(b'Initialized:      yes', hsm_out, 'softhsm2-tool failed: ' +
                      hsm_err.decode() + hsm_out.decode())
        self.assertIn(b'User PIN init.:   yes', hsm_out, 'softhsm2-tool failed: ' +
                      hsm_err.decode() + hsm_out.decode())

        # Check that pkcs11 output matches sofhsm output.
        p11_p = re.compile(r'Using slot [0-9] with a present token \((0x[0-9a-f]*)\)\s')
        p11_m = p11_p.search(p11_err.decode())
        self.assertTrue(p11_m, 'Slot number not found with pkcs11-tool: ' + p11_err.decode() + p11_out.decode())
        self.assertGreater(p11_m.lastindex, 0, 'Slot number not found with pkcs11-tool: ' +
                           p11_err.decode() + p11_out.decode())
        hsm_p = re.compile(r'Description:\s*SoftHSM slot ID (0x[0-9a-f]*)\s')
        hsm_m = hsm_p.search(hsm_out.decode())
        self.assertTrue(hsm_m, 'Slot number not found with softhsm2-tool: ' + hsm_err.decode() + hsm_out.decode())
        self.assertGreater(hsm_m.lastindex, 0, 'Slot number not found with softhsm2-tool: ' +
                           hsm_err.decode() + hsm_out.decode())
        self.assertEqual(p11_m.group(1), hsm_m.group(1), 'Slot number does not match: ' +
                         p11_err.decode() + p11_out.decode() + hsm_err.decode() + hsm_out.decode())

        verifyProvisioned(self, machine)

class SecondaryTests(OESelftestTestCase):
    @classmethod
    def setUpClass(cls):
        super(SecondaryTests, cls).setUpClass()
        logger = logging.getLogger("selftest")
        logger.info('Running bitbake to build secondary-image')
        bitbake('secondary-image')

    def setUpLocal(self):
        layer = "meta-updater-qemux86-64"
        result = runCmd('bitbake-layers show-layers')
        if re.search(layer, result.output) is None:
            # Assume the directory layout for finding other layers. We could also
            # make assumptions by using 'show-layers', but either way, if the
            # layers we need aren't where we expect them, we are out of like.
            path = os.path.abspath(os.path.dirname(__file__))
            metadir = path + "/../../../../../"
            self.meta_qemu = metadir + layer
            runCmd('bitbake-layers add-layer "%s"' % self.meta_qemu)
        else:
            self.meta_qemu = None
        self.append_config('MACHINE = "qemux86-64"')
        self.append_config('SOTA_CLIENT_PROV = " aktualizr-auto-prov "')
        self.qemu, self.s = qemu_launch(machine='qemux86-64', imagename='secondary-image')

    def tearDownLocal(self):
        qemu_terminate(self.s)
        if self.meta_qemu:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_qemu, ignore_status=True)

    def qemu_command(self, command):
        return qemu_send_command(self.qemu.ssh_port, command)

    def test_secondary_present(self):
        print('Checking aktualizr-secondary is present')
        stdout, stderr, retcode = self.qemu_command('aktualizr-secondary --help')
        self.assertEqual(retcode, 0, "Unable to run aktualizr-secondary --help")
        self.assertEqual(stderr, b'', 'Error: ' + stderr.decode())

    def test_secondary_listening(self):
        print('Checking aktualizr-secondary service is listening')
        stdout, stderr, retcode = self.qemu_command('echo test | nc localhost 9030')
        self.assertEqual(retcode, 0, "Unable to connect to secondary")


class PrimaryTests(OESelftestTestCase):
    @classmethod
    def setUpClass(cls):
        super(PrimaryTests, cls).setUpClass()
        logger = logging.getLogger("selftest")
        logger.info('Running bitbake to build primary-image')
        bitbake('primary-image')

    def setUpLocal(self):
        layer = "meta-updater-qemux86-64"
        result = runCmd('bitbake-layers show-layers')
        if re.search(layer, result.output) is None:
            # Assume the directory layout for finding other layers. We could also
            # make assumptions by using 'show-layers', but either way, if the
            # layers we need aren't where we expect them, we are out of like.
            path = os.path.abspath(os.path.dirname(__file__))
            metadir = path + "/../../../../../"
            self.meta_qemu = metadir + layer
            runCmd('bitbake-layers add-layer "%s"' % self.meta_qemu)
        else:
            self.meta_qemu = None
        self.append_config('MACHINE = "qemux86-64"')
        self.append_config('SOTA_CLIENT_PROV = " aktualizr-auto-prov "')
        self.append_config('SOTA_CLIENT_FEATURES = "secondary-network"')
        self.qemu, self.s = qemu_launch(machine='qemux86-64', imagename='primary-image')

    def tearDownLocal(self):
        qemu_terminate(self.s)
        if self.meta_qemu:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_qemu, ignore_status=True)

    def qemu_command(self, command):
        return qemu_send_command(self.qemu.ssh_port, command)

    def test_aktualizr_present(self):
        print('Checking aktualizr is present')
        stdout, stderr, retcode = self.qemu_command('aktualizr --help')
        self.assertEqual(retcode, 0, "Unable to run aktualizr --help")
        self.assertEqual(stderr, b'', 'Error: ' + stderr.decode())


def qemu_launch(efi=False, machine=None, imagename=None):
    logger = logging.getLogger("selftest")
    logger.info('Running bitbake to build core-image-minimal')
    bitbake('core-image-minimal')
    # Create empty object.
    args = type('', (), {})()
    if imagename:
        args.imagename = imagename
    else:
        args.imagename = 'core-image-minimal'
    args.mac = None
    # Could use DEPLOY_DIR_IMAGE here but it's already in the machine
    # subdirectory.
    args.dir = 'tmp/deploy/images'
    args.efi = efi
    args.machine = machine
    args.kvm = None  # Autodetect
    args.no_gui = True
    args.gdb = False
    args.pcap = None
    args.overlay = None
    args.dry_run = False
    args.secondary_network = False

    qemu = QemuCommand(args)
    cmdline = qemu.command_line()
    print('Booting image with run-qemu-ota...')
    s = subprocess.Popen(cmdline)
    sleep(10)
    return qemu, s


def qemu_terminate(s):
    try:
        s.terminate()
    except KeyboardInterrupt:
        pass


def qemu_send_command(port, command):
    command = ['ssh -q -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no root@localhost -p ' +
               str(port) + ' "' + command + '"']
    s2 = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    stdout, stderr = s2.communicate(timeout=60)
    return stdout, stderr, s2.returncode


def akt_native_run(testInst, cmd, **kwargs):
    # run a command supplied by aktualizr-native and checks that:
    # - the executable exists
    # - the command runs without error
    # NOTE: the base test class must have built aktualizr-native (in
    # setUpClass, for example)
    bb_vars = get_bb_vars(['SYSROOT_DESTDIR', 'base_prefix', 'libdir', 'bindir'],
                          'aktualizr-native')
    sysroot = bb_vars['SYSROOT_DESTDIR'] + bb_vars['base_prefix']
    sysrootbin = bb_vars['SYSROOT_DESTDIR'] + bb_vars['bindir']
    libdir = bb_vars['libdir']

    program, *_ = cmd.split(' ')
    p = '{}/{}'.format(sysrootbin, program)
    testInst.assertTrue(os.path.isfile(p), msg="No {} found ({})".format(program, p))
    env = dict(os.environ)
    env['LD_LIBRARY_PATH'] = libdir
    result = runCmd(cmd, env=env, native_sysroot=sysroot, ignore_status=True, **kwargs)
    testInst.assertEqual(result.status, 0, "Status not equal to 0. output: %s" % result.output)


def verifyProvisioned(testInst, machine):
    # Verify that device HAS provisioned.
    ran_ok = False
    for delay in [5, 5, 5, 5, 10, 10, 10, 10]:
        stdout, stderr, retcode = testInst.qemu_command('aktualizr-info')
        if retcode == 0 and stderr == b'' and stdout.decode().find('Fetched metadata: yes') >= 0:
            ran_ok = True
            break
        sleep(delay)
    testInst.assertIn(b'Device ID: ', stdout, 'Provisioning failed: ' + stderr.decode() + stdout.decode())
    testInst.assertIn(b'Primary ecu hardware ID: ' + machine.encode(), stdout,
                  'Provisioning failed: ' + stderr.decode() + stdout.decode())
    testInst.assertIn(b'Fetched metadata: yes', stdout, 'Provisioning failed: ' + stderr.decode() + stdout.decode())
    p = re.compile(r'Device ID: ([a-z0-9-]*)\n')
    m = p.search(stdout.decode())
    testInst.assertTrue(m, 'Device ID could not be read: ' + stderr.decode() + stdout.decode())
    testInst.assertGreater(m.lastindex, 0, 'Device ID could not be read: ' + stderr.decode() + stdout.decode())
    logger = logging.getLogger("selftest")
    logger.info('Device successfully provisioned with ID: ' + m.group(1))


# vim:set ts=4 sw=4 sts=4 expandtab:
