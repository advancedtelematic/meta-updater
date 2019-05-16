from os.path import exists, join, realpath, abspath
from os import listdir
import random
import socket
from subprocess import check_output, CalledProcessError

EXTENSIONS = {
    'intel-corei7-64': 'wic',
    'qemux86-64': 'ota-ext4'
}


def find_local_port(start_port):
    """"
    Find the next free TCP port after 'start_port'.
    """

    for port in range(start_port, start_port + 10):
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.bind(('', port))
            return port
        except socket.error:
            print("Skipping port %d" % port)
        finally:
            s.close()
    raise Exception("Could not find a free TCP port")


def random_mac():
    """Return a random Ethernet MAC address
    @link https://www.iana.org/assignments/ethernet-numbers/ethernet-numbers.xhtml#ethernet-numbers-2
    """
    head = "ca:fe:"
    hex_digits = '0123456789abcdef'
    tail = ':'.join([random.choice(hex_digits) + random.choice(hex_digits) for _ in range(4)])
    return head + tail


class QemuCommand(object):
    def __init__(self, args):
        if args.machine:
            self.machine = args.machine
        else:
            machines = listdir(args.dir)
            if len(machines) == 1:
                self.machine = machines[0]
            else:
                raise ValueError("Could not autodetect machine type. More than one entry in %s. Maybe --machine qemux86-64?" % args.dir)
        if args.efi:
            self.bios = 'OVMF.fd'
        else:
            uboot = abspath(join(args.dir, self.machine, 'u-boot-qemux86-64.rom'))
            if not exists(uboot):
                raise ValueError("U-Boot image %s does not exist" % uboot)
            self.bios = uboot
        if exists(args.imagename):
            image = args.imagename
        else:
            ext = EXTENSIONS.get(self.machine, 'wic')
            image = join(args.dir, self.machine, '%s-%s.%s' % (args.imagename, self.machine, ext))
        self.image = realpath(image)
        if not exists(self.image):
            raise ValueError("OS image %s does not exist" % self.image)
        if args.mac:
            self.mac_address = args.mac
        else:
            self.mac_address = random_mac()
        self.serial_port = find_local_port(8990)
        self.ssh_port = find_local_port(2222)
        if args.kvm is None:
            # Autodetect KVM using 'kvm-ok'
            try:
                check_output(['kvm-ok'])
                self.kvm = True
            except Exception:
                self.kvm = False
        else:
            self.kvm = args.kvm
        self.gui = not args.no_gui
        self.gdb = args.gdb
        self.pcap = args.pcap
        self.overlay = args.overlay
        self.secondary_network = args.secondary_network

    def command_line(self):
        netuser = 'user,hostfwd=tcp:0.0.0.0:%d-:22,restrict=off' % self.ssh_port
        if self.gdb:
            netuser += ',hostfwd=tcp:0.0.0.0:2159-:2159'
        cmdline = [
            "qemu-system-x86_64",
            "-bios", self.bios
        ]
        if not self.overlay:
            cmdline += ["-drive", "file=%s,if=ide,format=raw,snapshot=on" % self.image]
        cmdline += [
            "-serial", "tcp:127.0.0.1:%d,server,nowait" % self.serial_port,
            "-m", "1G",
            "-usb",
            "-object", "rng-random,id=rng0,filename=/dev/urandom",
            "-device", "virtio-rng-pci,rng=rng0",
            "-device", "usb-tablet",
            "-show-cursor",
            "-vga", "std",
            "-net", netuser,
            "-net", "nic,macaddr=%s" % self.mac_address
        ]
        if self.pcap:
            cmdline += ['-net', 'dump,file=' + self.pcap]
        if self.secondary_network:
            cmdline += [
                '-netdev', 'socket,id=vlan1,mcast=230.0.0.1:1234,localaddr=127.0.0.1',
                '-device', 'e1000,netdev=vlan1,mac='+random_mac(),
            ]
        if self.gui:
            cmdline += ["-serial", "stdio"]
        else:
            cmdline.append('-nographic')
        if self.kvm:
            cmdline += ['-enable-kvm', '-cpu', 'host']
        else:
            cmdline += ['-cpu', 'Haswell']
        if self.overlay:
            cmdline.append(self.overlay)
        return cmdline

    def img_command_line(self):
        cmdline = [
            "qemu-img", "create",
            "-o", "backing_file=%s" % self.image,
            "-f", "qcow2",
            self.overlay]
        return cmdline

