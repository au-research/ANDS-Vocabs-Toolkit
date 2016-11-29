#!/bin/bash

# The toolkit should be installed in a Tomcat container.
# Either edit this script to set the directories below,
# or set the environment variables catalina_base and
# TOOLKIT_ROOT to point to it.

: ${catalina_base:=~tomcat}
: ${TOOLKIT_ROOT:=~tomcat/webapps-ands/vocabtoolkit}

# NB, note use of custom logback configuration.
# It must not allow any logging to go to the normal Toolkit log!

SCRIPTPATH=$( cd $(dirname $0) ; pwd -P )

java -Dlogback.configurationFile=$SCRIPTPATH/logback-backup_poolparty.xml \
     -Dcatalina.base=${catalina_base} \
     -cp $TOOLKIT_ROOT/WEB-INF/lib/*:$TOOLKIT_ROOT/WEB-INF/classes \
     au.org.ands.vocabs.toolkit.provider.backup.PoolPartyBackupProvider \
     "$@"
