
do_install () {
  oe_runmake install DESTDIR=${D}${root_prefix}
}
