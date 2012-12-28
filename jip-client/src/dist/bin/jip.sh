#!/bin/bash

## CONFIGURATION
if [ -z "$JIP_MEM" ]; then
    JIP_MEM="3G"
fi

# test for a java installation in the path
if [ -z "`which java`" ]
then
    echo "No Java installation found, please install Java >= 1.6 and make sure it is in your PATH."
    exit 1
fi

# get the absolute path of the executable
SELF_PATH=$(cd -P -- "$(dirname -- "$0")" && pwd -P) && SELF_PATH=$SELF_PATH/$(basename -- "$0")

# resolve symlinks
while [ -h $SELF_PATH ]; do
    # 1) cd to directory of the symlink
    # 2) cd to the directory of where the symlink points
    # 3) get the pwd
    # 4) append the basename
    DIR=$(dirname -- "$SELF_PATH")
    SYM=$(readlink $SELF_PATH)
    SELF_PATH=$(cd $DIR && cd $(dirname -- "$SYM") && pwd)/$(basename -- "$SYM")
done
dir=`dirname $SELF_PATH`
libdir="$dir/../lib"

cp=""
for lib in $libdir/*
do
    if [ -n "$cp" ]; then
        cp=$lib:$cp
    else
        cp=$lib
    fi
done

MISC=""

if [ -n "$TMPDIR" ]; then
  MISC="-Djava.io.tmpdir="$TMPDIR
else
	if [ -n "$TMP_DIR" ]; then
  		MISC="-Djava.io.tmpdir="$TMP_DIR
	fi
fi

if [ -z "$JIP_HOME" ]; then
    JIP_HOME="${dir}/../"
fi

export PATH=$JIP_HOME/bin:$PATH

java -Xmx$JIP_MEM $MISC \
-Djip.home=$JIP_HOME \
${JAVA_OPTS} \
-cp $cp jip.Jip "$@"
