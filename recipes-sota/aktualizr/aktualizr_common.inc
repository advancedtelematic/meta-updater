SUMMARY = "Aktualizr SOTA Client"
DESCRIPTION = "SOTA Client application written in C++"
HOMEPAGE = "https://github.com/advancedtelematic/aktualizr"
SECTION = "base"
LICENSE = "MPL-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=9741c346eef56131163e13b9db1241b3"

PV = "1.0+git${SRCPV}"
PR = "7"

SRC_URI = " \
  git://github.com/advancedtelematic/aktualizr;branch=${BRANCH} \
  "
SRCREV = "1004efa3f86cef90c012b34620992b5762b741e3"
BRANCH ?= "master"

S = "${WORKDIR}/git"

inherit cmake
