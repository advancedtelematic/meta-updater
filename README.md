meta-sota
=========

This layer enables over-the-air updates with OSTree and RVI SOTA client.

[OSTree](https://github.com/ostreedev/ostree) is a tool for atomic full file
system upgrades with rollback capability. Main advantage of OSTree compared
to traditional dual partition model is that OSTree minimizes network bandwidth
and data storage footprint by sharing files with the same contents across file
system deployments.

[RVI SOTA client](https://github.com/advancedtelematic/rvi_sota_client) adds
authentication and provisioning capabilities to OTA and is integrated with
OSTree.

Build
-----

### Quickstart ###
[ATS Garage Quickstart](https://github.com/advancedtelematic/garage-quickstart-rpi)
is an example yocto-based project combining stadard poky distribution with
OSTree capabilities. For detailed getting started tutorial see [README](https://github.com/advancedtelematic/garage-quickstart-rpi/blob/master/README.adoc).

### Adding meta-updater capabilities to your build ###
If you already have a Yocto-base project and you want to add atomic filesystem
updates to it you need to do just three things:

1. Clone meta-updater layer and add it to your [conf/bblayers.conf](https://www.yoctoproject.org/docs/2.1/ref-manual/ref-manual.html#structure-build-conf-bblayers.conf).
2. Clone BSP integration layer (meta-updater-${PLATFORM}, e.g.
meta-updater-raspberrypi) and add it to your conf/bblayers.conf. If your board
isn't yet supported, you could write BSP integration work yourself. See [Supported boards](#supported-boards)
section for the details.
3. Set up your [distro](https://www.yoctoproject.org/docs/2.1/ref-manual/ref-manual.html#var-DISTRO).
If you are using "poky", the default distro in Yocto, you can change it in your
conf/local.conf to "poky-sota". Alternatively if you are using your own or third
party distro configuration, you can add 'require conf/distro/sota.conf.inc' to
it, thus combining capabilities of your distro with meta-updater features.

You can then build your image as usual (bitbake <your-image-name>). After building
the root file system bitbake will then create an [OSTree-enabled version](https://ostree.readthedocs.io/en/latest/manual/adapting-existing/)
of it, commit it to your local OSTree repo and optionally push it to a remote
server. Additionally a live disk image will be created (normally named
${IMAGE_NAME}.<boardpref>-sdimg-ota e.g. core-image-raspberrypi3.rpi-sdimg-ota).
You can control this behaviour though OSTree-related variables in your
local.conf, see [respective section](#sota-related-variables-in-localconf)
for details.

### Build with OpenIVI ###
### Build in AGL ###

With AGL you can just add agl-sota feature while configuring your build
environment as in

    source meta-agl/scripts/aglsetup.sh -m porter agl-demo agl-appfw-smack agl-devel agl-sota

you can then just run

    bitbake agl-demo-platform

and get as a result "ostree_repo" folder in your images directory
(tmp/deploy/images/${MACHINE}/ostree_repo) containing your OSTree repository
with rootfs committed as an OSTree deployment, 'otaimg' bootstrap image which
is an OSTree physical sysroot as a burnable filesystem image and optionally
some machine-dependent live images (e.g. '*.rpi-sdimg-ota' for Raspberry Pi or
'*.porter-sdimg-ota' Renesas Porter board).

Although aglsetup.sh hooks provide reasonable defaults for SOTA-related
variables you may want to tune some of them.

Supported boards
----------------

Currently supported platforms are

* [Raspberry Pi3](https://github.com/advancedtelematic/meta-updater-raspberrypi)
* [Minnowboard](https://github.com/advancedtelematic/meta-updater-minnowboard)
* [Native QEMU emulation](https://github.com/advancedtelematic/meta-updater-qemux86-64)

### Adding support for your board
If your board isn't yet supported you can add board integration code yourself.
The main purpose of this code is to provide a bootloader that will get use of
[OSTree's boot directory](https://ostree.readthedocs.io/en/latest/manual/atomic-upgrades/)
In meta-updater integration layers finished so far it is done by

1. Making the board boot into [U-Boot](http://www.denx.de/wiki/U-Boot)
2. Making U-boot import variables from /boot/loader/uEnv.txt and load the
kernel with initramfs and kernel command line arguments according to what is
set this file.

You may take a look into [Minnowboard](https://github.com/advancedtelematic/meta-updater-minnowboard)
or [Raspberry Pi](https://github.com/advancedtelematic/meta-updater-raspberrypi)
integration layers for examples.

It is still possible to make other loaders work with OSTree as well.

SOTA-related variables in local.conf
------------------------------------

* OSTREE_REPO - path to your OSTree repository.
  Defaults to "${DEPLOY_DIR_IMAGE}/ostree_repo"
* OSTREE_BRANCHNAME - the branch your rootfs will be committed to.
  Defaults to "ota"
* OSTREE_OSNAME - OS deployment name on your target device. For more
  information about deployments and osnames see
  [OSTree documentation](https://ostree.readthedocs.io/en/latest/manual/deployment/)
  Defaults to "poky".
* OSTREE_INITRAMFS_IMAGE - initramfs/initrd image that is used as a proxy while
  booting into OSTree deployment. Do not change this setting unless you are
  sure that your initramfs can serve as such proxy.
* OSTREE_PUSH_CREDENTIALS - when set adds pushing your ostree commit to a remote
  repo. sota-tools will then use credentials in the file pointed to by this
  variable.

Usage
-----

### OSTree ###
OSTree includes its own simple http server. It just exposes the whole OSTree
repository to the network so that any remote device can pull data from it to
device's local repository. To use OSTree http server you need OSTree installed
on your build machine. Alternatively, you could run version built inside Yocto
using bitbake's [devshell](http://www.openembedded.org/wiki/Devshell).

To expose your repo run ostree trivial-httpd using any free port.

    ostree trivial-httpd tmp/deploy/images/qemux86-64/ostree_repo -P 57556

You can then run from inside your device or QEMU emulation, provided your
network is set up correctly.

    # agl-remote identifies the remote server in your local repo
    ostree remote add --no-gpg-verify agl-remote http://192.168.7.1:57556 agl-ota
    
    # agl-ota is a branch name in the remote repo, set in OSTREE_BRANCHNAME
    ostree pull agl-remote agl-ota
    
    # agl is OS name as set in OSTREE_OSNAME
    ostree admin deploy --os=agl agl-remote:agl-ota

After restart you should boot into the newly deployed OS image.

E.g. for the raspberrypi3 you can try this sequence:

    # add remote
    ostree remote add --no-gpg-verify agl-snapshot https://download.automotivelinux.org/AGL/snapshots/master/latest/raspberrypi3/deploy/images/raspberrypi3/ostree_repo/ agl-ota
    
    # pull
    ostree pull agl-snapshot agl-ota
    
    # deploy
    ostree admin deploy --os=agl agl-snapshot:agl-ota

### SOTA tools ###
SOTA tools now contains only one tool, garage-push that lets you push the
changes in OSTree repository generated by bitbake process. It communicates with
an http server capable of querying files with HEAD requests and uploading them
with POST requests. garage-push is used as following:

    garage-push --repo=/path/to/ostree-repo --ref=mybranch --credentials=~/.sota-tools.json --user=username --password=

You can set OSTREE_PUSH_CREDENTIALS in your local.conf to make your build
results be automatically synchronized with a remote server.
Credentials are stored in JSON format which is described in [sota-tools documentation](https://github.com/advancedtelematic/sota-tools/blob/master/README.adoc)
