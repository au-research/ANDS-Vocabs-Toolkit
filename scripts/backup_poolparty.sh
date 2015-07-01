#!/bin/bash

# The toolkit should be installed in a Tomcat container.
# Either edit this script to set the directory below,
# or set the environment variable TOOLKIT_ROOT to point to it.

: ${TOOLKIT_ROOT:=~tomcat/webapps-ands/vocabtoolkit}

java -cp $TOOLKIT_ROOT/WEB-INF/lib/*:$TOOLKIT_ROOT/WEB-INF/classes \
     au.org.ands.vocabs.toolkit.provider.backup.PoolPartyBackupProvider \
     "$@"
