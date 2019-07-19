import re

from oeqa.selftest.case import OESelftestTestCase
from oeqa.utils.commands import runCmd, get_bb_var
from testutils import metadir, qemu_launch, qemu_send_command, qemu_terminate, verifyProvisioned


class MinnowTests(OESelftestTestCase):

    def setUpLocal(self):
        layer_intel = "meta-intel"
        layer_minnow = "meta-updater-minnowboard"
        result = runCmd('bitbake-layers show-layers')
        if re.search(layer_intel, result.output) is None:
            self.meta_intel = metadir() + layer_intel
            runCmd('bitbake-layers add-layer "%s"' % self.meta_intel)
        else:
            self.meta_intel = None
        if re.search(layer_minnow, result.output) is None:
            self.meta_minnow = metadir() + layer_minnow
            runCmd('bitbake-layers add-layer "%s"' % self.meta_minnow)
        else:
            self.meta_minnow = None
        self.append_config('MACHINE = "intel-corei7-64"')
        self.append_config('OSTREE_BOOTLOADER = "grub"')
        self.append_config('SOTA_CLIENT_PROV = " aktualizr-shared-prov "')
        self.qemu, self.s = qemu_launch(efi=True, machine='intel-corei7-64', mem='512M')

    def tearDownLocal(self):
        qemu_terminate(self.s)
        if self.meta_intel:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_intel, ignore_status=True)
        if self.meta_minnow:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_minnow, ignore_status=True)

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
                         '\nIs TianoCore ovmf installed on your host machine?')

        verifyProvisioned(self, machine)

# vim:set ts=4 sw=4 sts=4 expandtab:
