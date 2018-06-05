# ex:ts=4:sw=4:sts=4:et
# -*- tab-width: 4; c-basic-offset: 4; indent-tabs-mode: nil -*-
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License version 2 as
# published by the Free Software Foundation.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#

import logging
import os
import sys

from wic.plugins.source.rawcopy import SourcePlugin
from wic.misc import get_bitbake_var

logger = logging.getLogger('wic')

class OTAImagePlugin(SourcePlugin):
    """
    Add an OSTree enabled image rootfs to filesystem layout.
    This depends on otaimg image type, which in turn depends on ostree image type.
    """

    name = 'otaimage'

    @classmethod
    def do_prepare_partition(cls, part, source_params, cr, cr_workdir,
                             oe_builddir, bootimg_dir, kernel_dir,
                             rootfs_dir, native_sysroot):
        """
        Basically passes sysroot prepared by otaimg image type to Partition to
        create filesystem image from.
        """

        ota_sysroot = get_bitbake_var('OSTREE_IMAGE_SYSROOT')
        if not ota_sysroot:
            logger.error("Couldn't find OSTREE_IMAGE_SYSROOT, exiting")

        ota_sysroot = os.path.realpath(ota_sysroot)

        # prepare rootfs image from ota_sysroot
        part.prepare_rootfs(cr_workdir, oe_builddir, ota_sysroot, native_sysroot)
