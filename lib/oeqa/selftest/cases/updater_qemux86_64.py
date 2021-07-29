# pylint: disable=C0111,C0325
import os
import logging
import re
import subprocess
import unittest
from time import sleep
from uuid import uuid4

from oeqa.selftest.case import OESelftestTestCase
from oeqa.utils.commands import runCmd, bitbake, get_bb_var, get_bb_vars
from testutils import qemu_launch, qemu_send_command, qemu_terminate, \
    metadir, akt_native_run, verifyNotProvisioned, verifyProvisioned, \
    qemu_bake_image, qemu_boot_image


class GeneralTests(OESelftestTestCase):
    def test_credentials(self):
        logger = logging.getLogger("selftest")
        logger.info('Running bitbake to build core-image-minimal')
        self.append_config('SOTA_CLIENT_PROV = "aktualizr-shared-prov"')

        # note: this also tests ostreepush/garagesign/garagecheck which are
        # omitted from other test cases
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


class AktualizrToolsTests(OESelftestTestCase):

    @classmethod
    def setUpClass(cls):
        super(AktualizrToolsTests, cls).setUpClass()
        logger = logging.getLogger("selftest")
        logger.info('Running bitbake to build aktualizr-native tools')
        bitbake('aktualizr-native aktualizr-device-prov')
        bitbake('build-sysroots -c build_native_sysroot')

    def test_cert_provider_help(self):
        akt_native_run(self, 'aktualizr-cert-provider --help')

    def test_cert_provider_local_output(self):
        bb_vars = get_bb_vars(['SOTA_PACKED_CREDENTIALS', 'T'], 'aktualizr-native')
        creds = bb_vars['SOTA_PACKED_CREDENTIALS']
        temp_dir = bb_vars['T']
        bb_vars_prov = get_bb_vars(['WORKDIR', 'libdir'], 'aktualizr-device-prov')
        config = bb_vars_prov['WORKDIR'] + '/sysroot-destdir' + bb_vars_prov['libdir'] + '/sota/conf.d/20-sota-device-cred.toml'

        akt_native_run(self, 'aktualizr-cert-provider -c {creds} -r -l {temp} -g {config}'
                       .format(creds=creds, temp=temp_dir, config=config))

        # Might be nice if these names weren't hardcoded.
        cert_path = temp_dir + '/var/sota/import/client.pem'
        self.assertTrue(os.path.isfile(cert_path), "Client certificate not found at %s." % cert_path)
        self.assertTrue(os.path.getsize(cert_path) > 0, "Client certificate at %s is empty." % cert_path)
        pkey_path = temp_dir + '/var/sota/import/pkey.pem'
        self.assertTrue(os.path.isfile(pkey_path), "Private key not found at %s." % pkey_path)
        self.assertTrue(os.path.getsize(pkey_path) > 0, "Private key at %s is empty." % pkey_path)
        ca_path = temp_dir + '/var/sota/import/root.crt'
        self.assertTrue(os.path.isfile(ca_path), "Client certificate not found at %s." % ca_path)
        self.assertTrue(os.path.getsize(ca_path) > 0, "Client certificate at %s is empty." % ca_path)


class SharedCredProvTests(OESelftestTestCase):

    def setUpLocal(self):
        layer = "meta-updater-qemux86-64"
        result = runCmd('bitbake-layers show-layers')
        if re.search(layer, result.output) is None:
            self.meta_qemu = metadir() + layer
            runCmd('bitbake-layers add-layer "%s"' % self.meta_qemu)
        else:
            self.meta_qemu = None
        self.append_config('MACHINE = "qemux86-64"')
        self.append_config('SOTA_CLIENT_PROV = " aktualizr-shared-prov "')
        self.append_config('IMAGE_FSTYPES:remove = "ostreepush garagesign garagecheck"')
        self.append_config('SOTA_HARDWARE_ID = "plain_reibekuchen_314"')
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

        hwid = get_bb_var('SOTA_HARDWARE_ID')
        verifyProvisioned(self, machine, hwid)


class SharedCredProvTestsNonOSTree(SharedCredProvTests):

    def setUpLocal(self):
        layer = "meta-updater-qemux86-64"
        result = runCmd('bitbake-layers show-layers')
        if re.search(layer, result.output) is None:
            self.meta_qemu = metadir() + layer
            runCmd('bitbake-layers add-layer "%s"' % self.meta_qemu)
        else:
            self.meta_qemu = None
        self.append_config('MACHINE = "qemux86-64"')
        self.append_config('SOTA_CLIENT_PROV = ""')
        self.append_config('IMAGE_FSTYPES:remove = "ostreepush garagesign garagecheck"')
        self.append_config('SOTA_HARDWARE_ID = "plain_reibekuchen_314"')

        self.append_config('DISTRO = "poky"')
        self.append_config('DISTRO_FEATURES:append = " systemd"')
        self.append_config('VIRTUAL-RUNTIME_init_manager = "systemd"')
        self.append_config('PREFERRED_RPROVIDER_network-configuration ??= "networkd-dhcp-conf"')
        self.append_config('PACKAGECONFIG:pn-aktualizr = ""')
        self.append_config('SOTA_DEPLOY_CREDENTIALS = "1"')
        self.append_config('IMAGE_INSTALL:append += "aktualizr aktualizr-info aktualizr-shared-prov"')
        self.qemu, self.s = qemu_launch(machine='qemux86-64', uboot_enable='no')


class ManualControlTests(OESelftestTestCase):

    def setUpLocal(self):
        layer = "meta-updater-qemux86-64"
        result = runCmd('bitbake-layers show-layers')
        if re.search(layer, result.output) is None:
            self.meta_qemu = metadir() + layer
            runCmd('bitbake-layers add-layer "%s"' % self.meta_qemu)
        else:
            self.meta_qemu = None
        self.append_config('MACHINE = "qemux86-64"')
        self.append_config('SOTA_CLIENT_PROV = " aktualizr-shared-prov "')
        self.append_config('SYSTEMD_AUTO_ENABLE:aktualizr = "disable"')
        self.append_config('IMAGE_FSTYPES:remove = "ostreepush garagesign garagecheck"')
        self.qemu, self.s = qemu_launch(machine='qemux86-64')

    def tearDownLocal(self):
        qemu_terminate(self.s)
        if self.meta_qemu:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_qemu, ignore_status=True)

    def qemu_command(self, command):
        return qemu_send_command(self.qemu.ssh_port, command)

    def test_manual_run_mode_once(self):
        """
        Disable the systemd service then run aktualizr manually
        """
        sleep(20)
        stdout, stderr, retcode = self.qemu_command('aktualizr-info')
        self.assertIn(b'Can\'t open database', stderr,
                      'Aktualizr should not have run yet' + stderr.decode() + stdout.decode())

        stdout, stderr, retcode = self.qemu_command('aktualizr once')

        stdout, stderr, retcode = self.qemu_command('aktualizr-info')
        self.assertIn(b'Fetched metadata: yes', stdout,
                      'Aktualizr should have run' + stderr.decode() + stdout.decode())


class DeviceCredProvTests(OESelftestTestCase):

    def setUpLocal(self):
        layer = "meta-updater-qemux86-64"
        result = runCmd('bitbake-layers show-layers')
        if re.search(layer, result.output) is None:
            self.meta_qemu = metadir() + layer
            runCmd('bitbake-layers add-layer "%s"' % self.meta_qemu)
        else:
            self.meta_qemu = None
        self.append_config('MACHINE = "qemux86-64"')
        self.append_config('SOTA_CLIENT_PROV = " aktualizr-device-prov "')
        self.append_config('SOTA_DEPLOY_CREDENTIALS = "0"')
        self.append_config('IMAGE_FSTYPES:remove = "ostreepush garagesign garagecheck"')
        self.qemu, self.s = qemu_launch(machine='qemux86-64')
        bitbake('build-sysroots -c build_native_sysroot')

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

        verifyNotProvisioned(self, machine)

        # Run aktualizr-cert-provider.
        bb_vars = get_bb_vars(['SOTA_PACKED_CREDENTIALS'], 'aktualizr-native')
        creds = bb_vars['SOTA_PACKED_CREDENTIALS']
        bb_vars_prov = get_bb_vars(['WORKDIR', 'libdir'], 'aktualizr-device-prov')
        config = bb_vars_prov['WORKDIR'] + '/sysroot-destdir' + bb_vars_prov['libdir'] + '/sota/conf.d/20-sota-device-cred.toml'

        print('Provisining at root@localhost:%d' % self.qemu.ssh_port)
        akt_native_run(self, 'aktualizr-cert-provider -c {creds} -t root@localhost -p {port} -s -u -r -g {config}'
                       .format(creds=creds, port=self.qemu.ssh_port, config=config))

        verifyProvisioned(self, machine)


class DeviceCredProvHsmTests(OESelftestTestCase):

    def setUpLocal(self):
        layer = "meta-updater-qemux86-64"
        result = runCmd('bitbake-layers show-layers')
        if re.search(layer, result.output) is None:
            self.meta_qemu = metadir() + layer
            runCmd('bitbake-layers add-layer "%s"' % self.meta_qemu)
        else:
            self.meta_qemu = None
        self.append_config('MACHINE = "qemux86-64"')
        self.append_config('SOTA_CLIENT_PROV = "aktualizr-device-prov-hsm"')
        self.append_config('SOTA_DEPLOY_CREDENTIALS = "0"')
        self.append_config('SOTA_CLIENT_FEATURES = "hsm"')
        self.append_config('IMAGE_INSTALL:append = " softhsm-testtoken"')
        self.append_config('IMAGE_FSTYPES:remove = "ostreepush garagesign garagecheck"')
        self.qemu, self.s = qemu_launch(machine='qemux86-64')
        bitbake('build-sysroots -c build_native_sysroot')

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

        verifyNotProvisioned(self, machine)

        # Verify that HSM is not yet initialized.
        pkcs11_command = 'pkcs11-tool --module=/usr/lib/softhsm/libsofthsm2.so -O'
        stdout, stderr, retcode = self.qemu_command(pkcs11_command)
        self.assertNotEqual(retcode, 0, 'pkcs11-tool succeeded before initialization: ' +
                            stdout.decode() + stderr.decode())
        softhsm2_command = 'softhsm2-util --show-slots'
        stdout, stderr, retcode = self.qemu_command(softhsm2_command)
        self.assertNotEqual(retcode, 0, 'softhsm2-tool succeeded before initialization: ' +
                            stdout.decode() + stderr.decode())

        # Run aktualizr-cert-provider.
        bb_vars = get_bb_vars(['SOTA_PACKED_CREDENTIALS'], 'aktualizr-native')
        creds = bb_vars['SOTA_PACKED_CREDENTIALS']
        bb_vars_prov = get_bb_vars(['WORKDIR', 'libdir'], 'aktualizr-device-prov-hsm')
        config = bb_vars_prov['WORKDIR'] + '/sysroot-destdir' + bb_vars_prov['libdir'] + '/sota/conf.d/20-sota-device-cred-hsm.toml'

        akt_native_run(self, 'aktualizr-cert-provider -c {creds} -t root@localhost -p {port} -r -s -u -g {config}'
                       .format(creds=creds, port=self.qemu.ssh_port, config=config))

        # Verify that HSM is able to initialize.
        for delay in [5, 5, 5, 5, 10]:
            sleep(delay)
            p11_out, p11_err, p11_ret = self.qemu_command(pkcs11_command)
            hsm_out, hsm_err, hsm_ret = self.qemu_command(softhsm2_command)
            if (p11_ret == 0 and hsm_ret == 0 and hsm_err == b'' and
                    b'X.509 cert' in p11_out and b'present token' in p11_err):
                break
        else:
            self.fail('pkcs11-tool or softhsm2-tool failed: ' + p11_err.decode() +
                      p11_out.decode() + hsm_err.decode() + hsm_out.decode())

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


class IpSecondaryTests(OESelftestTestCase):

    class Image:
        def __init__(self, imagename, binaryname, machine='qemux86-64', bake=True, **kwargs):
            self.machine = machine
            self.imagename = imagename
            self.boot_kwargs = kwargs
            self.binaryname = binaryname
            self.stdout = ''
            self.stderr = ''
            self.retcode = 0
            if bake:
                self.bake()

        def bake(self):
            self.configure()
            qemu_bake_image(self.imagename)

        def send_command(self, cmd, timeout=60):
            stdout, stderr, retcode = qemu_send_command(self.qemu.ssh_port, cmd, timeout=timeout)
            return str(stdout), str(stderr), retcode

        def __enter__(self):
            self.qemu, self.process = qemu_boot_image(machine=self.machine, imagename=self.imagename,
                                                      wait_for_boot_time=1, **self.boot_kwargs)
            # wait until the VM is booted and is SSHable
            self.wait_till_sshable()

        def __exit__(self, exc_type, exc_val, exc_tb):
            qemu_terminate(self.process)

        def wait_till_sshable(self):
            # qemu_send_command tries to ssh into the qemu VM and blocks until it gets there or timeout happens
            # so it helps us to block q control flow until the VM is booted and a target binary/daemon is running there
            self.stdout, self.stderr, self.retcode = self.send_command(self.binaryname + ' --help', timeout=300)

        def was_successfully_booted(self):
            return self.retcode == 0

    class Secondary(Image):
        def __init__(self, test_ctx):
            self._test_ctx = test_ctx
            self.sndry_serial = str(uuid4())
            self.sndry_hw_id = 'qemux86-64-oeselftest-sndry'
            self.id = (self.sndry_hw_id, self.sndry_serial)
            super(IpSecondaryTests.Secondary, self).__init__('secondary-image', 'aktualizr-secondary',
                                                             secondary_network=True)

        def configure(self):
            self._test_ctx.append_config('SECONDARY_SERIAL_ID = "{}"'.format(self.sndry_serial))
            self._test_ctx.append_config('SECONDARY_HARDWARE_ID = "{}"'.format(self.sndry_hw_id))

    class Primary(Image):
        def __init__(self, test_ctx):
            self._test_ctx = test_ctx
            super(IpSecondaryTests.Primary, self).__init__('primary-image', 'aktualizr', secondary_network=True)

        def configure(self):
            self._test_ctx.append_config('MACHINE = "qemux86-64"')
            self._test_ctx.append_config('SOTA_CLIENT_PROV = " aktualizr-shared-prov "')

        def is_ecu_registered(self, ecu_id):
            device_status = self.get_info()

            if not ((device_status.find(ecu_id[0]) != -1) and (device_status.find(ecu_id[1]) != -1)):
                return False
            not_registered_field = "Removed or not registered ecus:"
            not_reg_start = device_status.find(not_registered_field)
            return not_reg_start == -1 or (device_status.find(ecu_id[1], not_reg_start) == -1)

        def get_info(self):
            stdout, stderr, retcode = self.send_command('aktualizr-info --wait-until-provisioned', timeout=620)
            self._test_ctx.assertEqual(retcode, 0, 'Unable to run aktualizr-info: {}'.format(stderr))
            return stdout

    def setUpLocal(self):
        layer = "meta-updater-qemux86-64"
        result = runCmd('bitbake-layers show-layers')
        if re.search(layer, result.output) is None:
            self.meta_qemu = metadir() + layer
            runCmd('bitbake-layers add-layer "%s"' % self.meta_qemu)
        else:
            self.meta_qemu = None

        self.append_config('IMAGE_FSTYPES:remove = "ostreepush garagesign garagecheck"')
        self.primary = IpSecondaryTests.Primary(self)
        self.secondary = IpSecondaryTests.Secondary(self)

    def tearDownLocal(self):
        if self.meta_qemu:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_qemu, ignore_status=True)

    def test_ip_secondary_registration_if_secondary_starts_first(self):
        with self.secondary:
            self.assertTrue(self.secondary.was_successfully_booted(),
                            'The secondary failed to boot: {}'.format(self.secondary.stderr))

            with self.primary:
                self.assertTrue(self.primary.was_successfully_booted(),
                                'The primary failed to boot: {}'.format(self.primary.stderr))

                self.assertTrue(self.primary.is_ecu_registered(self.secondary.id),
                                "The secondary wasn't registered at the primary: {}".format(self.primary.get_info()))

    def test_ip_secondary_registration_if_primary_starts_first(self):
        with self.primary:
            self.assertTrue(self.primary.was_successfully_booted(),
                            'The primary failed to boot: {}'.format(self.primary.stderr))

            with self.secondary:
                self.assertTrue(self.secondary.was_successfully_booted(),
                                'The secondary failed to boot: {}'.format(self.secondary.stderr))

                self.assertTrue(self.primary.is_ecu_registered(self.secondary.id),
                                "The secondary wasn't registered at the primary: {}".format(self.primary.get_info()))


class ResourceControlTests(OESelftestTestCase):
    def setUpLocal(self):
        layer = "meta-updater-qemux86-64"
        result = runCmd('bitbake-layers show-layers')
        if re.search(layer, result.output) is None:
            self.meta_qemu = metadir() + layer
            runCmd('bitbake-layers add-layer "%s"' % self.meta_qemu)
        else:
            self.meta_qemu = None
        self.append_config('MACHINE = "qemux86-64"')
        self.append_config('SOTA_CLIENT_PROV = " aktualizr-shared-prov "')
        self.append_config('IMAGE_FSTYPES:remove = "ostreepush garagesign garagecheck"')
        self.append_config('IMAGE_INSTALL:append += " aktualizr-resource-control "')
        self.append_config('RESOURCE_CPU_WEIGHT:pn-aktualizr = "1000"')
        self.append_config('RESOURCE_MEMORY_HIGH:pn-aktualizr = "50M"')
        self.append_config('RESOURCE_MEMORY_MAX:pn-aktualizr = "1M"')
        self.qemu, self.s = qemu_launch(machine='qemux86-64')

    def tearDownLocal(self):
        qemu_terminate(self.s)
        if self.meta_qemu:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_qemu, ignore_status=True)

    def qemu_command(self, command):
        return qemu_send_command(self.qemu.ssh_port, command)

    def test_aktualizr_resource_control(self):
        print('Checking aktualizr was killed')
        ran_ok = False
        for delay in [5, 5, 5, 5]:
            sleep(delay)
            try:
                stdout, stderr, retcode = self.qemu_command('systemctl --no-pager show aktualizr')
                if retcode == 0 and b'ExecMainStatus=9' in stdout:
                    ran_ok = True
                    break
            except subprocess.TimeoutExpired:
                pass
        self.assertTrue(ran_ok, 'Aktualizr was not killed')

        self.assertIn(b'CPUWeight=1000', stdout, 'CPUWeight was not set correctly')
        self.assertIn(b'MemoryHigh=52428800', stdout, 'MemoryHigh was not set correctly')
        self.assertIn(b'MemoryMax=1048576', stdout, 'MemoryMax was not set correctly')

        self.qemu_command('systemctl --runtime set-property aktualizr MemoryMax=')
        self.qemu_command('systemctl restart aktualizr')

        stdout, stderr, retcode = self.qemu_command('systemctl --no-pager show --property=ExecMainStatus aktualizr')
        self.assertIn(b'ExecMainStatus=0', stdout, 'Aktualizr did not restart')


class NonSystemdTests(OESelftestTestCase):
    def setUpLocal(self):
        layer = "meta-updater-qemux86-64"
        result = runCmd('bitbake-layers show-layers')
        if re.search(layer, result.output) is None:
            self.meta_qemu = metadir() + layer
            runCmd('bitbake-layers add-layer "%s"' % self.meta_qemu)
        else:
            self.meta_qemu = None
        self.append_config('MACHINE = "qemux86-64"')
        self.append_config('SOTA_CLIENT_PROV = " aktualizr-shared-prov "')
        self.append_config('IMAGE_FSTYPES:remove = "ostreepush garagesign garagecheck"')
        self.append_config('DISTRO = "poky-sota"')
        self.append_config('IMAGE_INSTALL:remove += " aktualizr-resource-control"')
        self.qemu, self.s = qemu_launch(machine='qemux86-64')

    def tearDownLocal(self):
        qemu_terminate(self.s)
        if self.meta_qemu:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_qemu, ignore_status=True)

    def qemu_command(self, command):
        return qemu_send_command(self.qemu.ssh_port, command)

    def test_provisioning(self):
        print('Checking if systemd is not installed...')
        stdout, stderr, retcode = self.qemu_command('systemctl')
        self.assertTrue(retcode != 0, 'systemd is installed while it is not supposed to: ' + str(stdout))

        stdout, stderr, retcode = self.qemu_command('aktualizr --run-mode once')
        self.assertEqual(retcode, 0, 'Failed to run aktualizr: ' + str(stdout) + str(stderr))

        machine = get_bb_var('MACHINE', 'core-image-minimal')
        verifyProvisioned(self, machine)

# vim:set ts=4 sw=4 sts=4 expandtab:
