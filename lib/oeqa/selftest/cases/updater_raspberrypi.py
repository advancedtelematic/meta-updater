# pylint: disable=C0111,C0325
import os
import logging
import re
import unittest

from oeqa.selftest.case import OESelftestTestCase
from oeqa.utils.commands import runCmd, bitbake, get_bb_var

from testutils import metadir


class RpiTests(OESelftestTestCase):

    def setUpLocal(self):
        # Add layers before changing the machine type, otherwise the sanity
        # checker complains loudly.
        layer:rpi = "meta-raspberrypi"
        layer_upd:rpi = "meta-updater-raspberrypi"
        result = runCmd('bitbake-layers show-layers')
        if re.search(layer_rpi, result.output) is None:
            self.meta_rpi = metadir() + layer:rpi
            runCmd('bitbake-layers add-layer "%s"' % self.meta_rpi)
        else:
            self.meta_rpi = None
        if re.search(layer_upd_rpi, result.output) is None:
            self.meta_upd_rpi = metadir() + layer_upd:rpi
            runCmd('bitbake-layers add-layer "%s"' % self.meta_upd_rpi)
        else:
            self.meta_upd_rpi = None

        self.append_config('MACHINE = "raspberrypi3"')
        self.append_config('SOTA_CLIENT_PROV = " aktualizr-shared-prov "')

    def tearDownLocal(self):
        if self.meta_upd_rpi:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_upd_rpi, ignore_status=True)
        if self.meta_rpi:
            runCmd('bitbake-layers remove-layer "%s"' % self.meta_rpi, ignore_status=True)

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
