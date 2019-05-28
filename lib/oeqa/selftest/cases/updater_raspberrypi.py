# pylint: disable=C0111,C0325
import os
import logging
import re
import unittest

from oeqa.selftest.case import OESelftestTestCase
from oeqa.utils.commands import runCmd, bitbake, get_bb_var


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
        # layers we need aren't where we expect them, we are out of luck.
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
        self.append_config('SOTA_CLIENT_PROV = " aktualizr-shared-prov "')

    def tearDownLocal(self):
        if self.meta_qemu:
            runCmd('bitbake-layers add-layer "%s"' % self.meta_qemu, ignore_status=True)
        if self.meta_upd_rpi:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_upd_rpi, ignore_status=True)
        if self.meta_rpi:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_rpi, ignore_status=True)
        if self.meta_python:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_python, ignore_status=True)

    def test_build(self):
        logger = logging.getLogger("selftest")
        logger.info('Running bitbake to build core-image-minimal')
        bitbake('core-image-minimal')
        credentials = get_bb_var('SOTA_PACKED_CREDENTIALS')
        # Skip the test if the variable SOTA_PACKED_CREDENTIALS is not set.
        if credentials is None:
            raise unittest.SkipTest("Variable 'SOTA_PACKED_CREDENTIALS' not set.")
        # Check if the file exists.
        self.assertTrue(os.path.isfile(credentials), "File %s does not exist" % credentials)
        deploydir = get_bb_var('DEPLOY_DIR_IMAGE')
        imagename = get_bb_var('IMAGE_LINK_NAME', 'core-image-minimal')
        # Check if the credentials are included in the output image.
        result = runCmd('tar -jtvf %s/%s.tar.bz2 | grep sota_provisioning_credentials.zip' %
                        (deploydir, imagename), ignore_status=True)
        self.assertEqual(result.status, 0, "Status not equal to 0. output: %s" % result.output)

# vim:set ts=4 sw=4 sts=4 expandtab:
