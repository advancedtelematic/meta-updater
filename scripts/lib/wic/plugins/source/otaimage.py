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

from wic.plugins.source.rawcopy import RawCopyPlugin
from wic.utils.misc import get_bitbake_var

logger = logging.getLogger('wic')

class OTAImagePlugin(RawCopyPlugin):
    """
    Add an already existing filesystem image to the partition layout.
    """

    name = 'otaimage'

    @classmethod
    def do_prepare_partition(cls, part, source_params, cr, cr_workdir,
                             oe_builddir, bootimg_dir, kernel_dir,
                             rootfs_dir, native_sysroot):
        """
        Called to do the actual content population for a partition i.e. it
        'prepares' the partition to be incorporated into the image.
        """
        bootimg_dir = get_bitbake_var("DEPLOY_DIR_IMAGE")
        if not bootimg_dir:
            logger.error("Couldn't find DEPLOY_DIR_IMAGE, exiting\n")

        logger.debug('Bootimg dir: %s' % bootimg_dir)

        src = bootimg_dir + "/" + get_bitbake_var("IMAGE_LINK_NAME") + ".otaimg"

        logger.debug('Preparing partition using image %s' % (src))
        source_params['file'] = src

        super(OTAImagePlugin, cls).do_prepare_partition(part, source_params,
                                                         cr, cr_workdir, oe_builddir,
                                                         bootimg_dir, kernel_dir,
                                                         rootfs_dir, native_sysroot)

