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

