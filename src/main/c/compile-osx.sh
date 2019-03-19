#!/bin/bash

# Modify as you need.
MBEDTLS_ROOT=mbedtl
CFLAGS="-O3 -I$JAVA_HOME/include/ -I$JAVA_HOME/include/darwin/ -fPIC -shared"

clang $CFLAGS -lz  MacOSDarkModeImpl.m -fobjc-arc -fmodules -mmacosx-version-min=10.6 -o MacOSDarkModeImpl.m.dylib