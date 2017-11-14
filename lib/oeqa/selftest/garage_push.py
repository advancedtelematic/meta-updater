import unittest
import os
import logging

from oeqa.selftest.base import oeSelfTest
from oeqa.utils.commands import runCmd, bitbake, get_bb_var

class GaragePushTests(oeSelfTest):

    @classmethod
    def setUpClass(cls):
        # Ensure we have the right data in pkgdata
        logger = logging.getLogger("selftest")
        logger.info('Running bitbake to build aktualizr-native tools')
        bitbake('aktualizr-native garage-sign-native')

    def test_help(self):
        image_dir = get_bb_var("D", "aktualizr-native")
        bin_dir = get_bb_var("bindir", "aktualizr-native")
        gp_path = os.path.join(image_dir, bin_dir[1:], 'garage-push')
        result = runCmd('%s --help' % gp_path, ignore_status=True)
        self.assertEqual(result.status, 0, "Status not equal to 0. output: %s" % result.output)

    def test_java(self):
        result = runCmd('which java', ignore_status=True)
        self.assertEqual(result.status, 0, "Java not found.")

    def test_sign(self):
        image_dir = get_bb_var("D", "garage-sign-native")
        bin_dir = get_bb_var("bindir", "garage-sign-native")
        gs_path = os.path.join(image_dir, bin_dir[1:], 'garage-sign')
        result = runCmd('%s --help' % gs_path, ignore_status=True)
        self.assertEqual(result.status, 0, "Status not equal to 0. output: %s" % result.output)

    def test_push(self):
        bitbake('core-image-minimal')
        self.write_config('IMAGE_INSTALL_append = " man "')
        bitbake('core-image-minimal')

    def test_hsm(self):
        self.write_config('SOTA_CLIENT_FEATURES="hsm hsm-test"')
        bitbake('core-image-minimal')

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
        imagename = get_bb_var('IMAGE_LINK_NAME', target='core-image-minimal')
        # Check if the credentials are included in the output image
        result = runCmd('tar -jtvf %s/%s.tar.bz2 | grep sota_provisioning_credentials.zip' % (deploydir, imagename), ignore_status=True)
        self.assertEqual(result.status, 0, "Status not equal to 0. output: %s" % result.output)
