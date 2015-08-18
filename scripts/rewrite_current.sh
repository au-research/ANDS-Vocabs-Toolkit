#!/bin/bash

# The toolkit should be installed in a Tomcat container.
# Either edit this script to set the directory below,
# or set the environment variable TOOLKIT_ROOT to point to it.

: ${TOOLKIT_ROOT:=~tomcat/webapps-ands/vocabtoolkit}

# NB, note use of custom logback configuration.
# It must not allow any logging to go to stdout.
# Just in case, redirect stderr to /dev/null. But
# the logback configuration should be enough.

SCRIPTPATH=$( cd $(dirname $0) ; pwd -P )

java -Dlogback.configurationFile=$SCRIPTPATH/logback-redirect_current.xml \
     -cp $TOOLKIT_ROOT/WEB-INF/lib/*:$TOOLKIT_ROOT/WEB-INF/classes \
     au.org.ands.vocabs.toolkit.utils.RewriteCurrent \
     "$@" 2>/dev/null
