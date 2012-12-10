#!/bin/bash
#
# JIP Boostrap script
#

## CONFIGURATION
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
if [ -z "$JIP_HOME" ]; then
    JIP_HOME="${dir}/../"
fi


echo "Bootstrapping JIP environment"
echo "-----------------------------"
echo "Target : ${homedir}"
echo "-----------------------------"
echo ""
echo "Preparing virtual environment"
python ${dir}/../scripts/virtualenv.py -p python2.7 ${JIP_HOME}


