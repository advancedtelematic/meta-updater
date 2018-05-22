SUMMARY = "ASN.1 to C compiler"
DESCRIPTION = "Generates serialization routines from ASN.1 schemas"
HOMEPAGE = "http://lionet.info/asn1c"
SECTION = "base"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=ee8bfaaa7d71cf3edb079475e6716d4b"

inherit autotools native

PV = "0.9.28"
SRC_URI = "https://github.com/vlm/asn1c/releases/download/v${PV}/asn1c-${PV}.tar.gz \
           file://skeletons_dir_fix.patch"
SRC_URI[sha256sum] = "8007440b647ef2dd9fb73d931c33ac11764e6afb2437dbe638bb4e5fc82386b9"

BBCLASSEXTEND = "native nativesdk"

# vim:set ts=4 sw=4 sts=4 expandtab:
