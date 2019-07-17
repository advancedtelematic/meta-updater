# pylint: disable=C0111,C0325
import logging

from oeqa.selftest.case import OESelftestTestCase
from oeqa.utils.commands import runCmd, bitbake, get_bb_var
from testutils import akt_native_run


class SotaToolsTests(OESelftestTestCase):

    @classmethod
    def setUpClass(cls):
        super(SotaToolsTests, cls).setUpClass()
        logger = logging.getLogger("selftest")
        logger.info('Running bitbake to build aktualizr-native tools')
        bitbake('aktualizr-native')
        bitbake('build-sysroots -c build_native_sysroot')

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

    def test_feature_usrmerge(self):
        result = get_bb_var('DISTRO_FEATURES').find('usrmerge')
        self.assertNotEqual(result, -1, 'Feature "sota" not set at DISTRO_FEATURES')

    def test_feature_systemd(self):
        result = get_bb_var('DISTRO_FEATURES').find('systemd')
        self.assertNotEqual(result, -1, 'Feature "systemd" not set at DISTRO_FEATURES')

    def test_java(self):
        result = runCmd('which java', ignore_status=True)
        self.assertEqual(result.status, 0,
                         "Java not found. Do you have a JDK installed on your host machine?")

# vim:set ts=4 sw=4 sts=4 expandtab:
