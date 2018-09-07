FROM debian:stable
LABEL Description="Image for bitbaking"

RUN sed -i 's#deb http://deb.debian.org/debian stable main#deb http://deb.debian.org/debian stable main contrib#g' /etc/apt/sources.list
RUN sed -i 's#deb http://deb.debian.org/debian stable-updates main#deb http://deb.debian.org/debian stable-updates main contrib#g' /etc/apt/sources.list
RUN apt-get update -q && apt-get install -qy \
     build-essential \
     bzip2 \
     chrpath \
     cpio \
     default-jre \
     diffstat \
     gawk \
     gcc-multilib \
     git-core \
     iputils-ping \
     iproute \
     libpython-dev \
     libsdl1.2-dev \
     locales \
     ovmf \
     procps \
     python \
     python3 \
     python3-pexpect \
     qemu \
     socat \
     texinfo \
     unzip \
     wget \
     xterm \
     xz-utils

ARG uid=1000
ARG gid=1000
RUN groupadd -g $gid bitbake
RUN useradd -m -u $uid -g $gid bitbake

RUN echo "en_US.UTF-8 UTF-8" > /etc/locale.gen && locale-gen
ENV LC_ALL="en_US.UTF-8"
ENV LANG="en_US.UTF-8"
ENV LANGUAGE="en_US.UTF-8"
