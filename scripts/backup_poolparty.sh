#!/bin/bash

# The toolkit should be installed in a Tomcat container.
# Either edit this script to set the directories below,
# or set the environment variables catalina_base and
# TOOLKIT_ROOT to point to it.

: ${catalina_base:=~tomcat}
: ${TOOLKIT_ROOT:=~tomcat/webapps-ands/vocabtoolkit}

java -Dcatalina.base=${catalina_base} \
     -cp $TOOLKIT_ROOT/WEB-INF/lib/*:$TOOLKIT_ROOT/WEB-INF/classes \
     au.org.ands.vocabs.toolkit.provider.backup.PoolPartyBackupProvider \
     "$@"
