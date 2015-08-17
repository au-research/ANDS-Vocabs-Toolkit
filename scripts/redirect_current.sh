#!/bin/bash

# The toolkit should be installed in a Tomcat container.
# Either edit this script to set the directories below,
# or set the environment variables catalina_base and
# TOOLKIT_ROOT to point to it.

: ${catalina_base:=~tomcat}
: ${TOOLKIT_ROOT:=~tomcat/webapps-ands/vocabtoolkit}

# Although the main() method disables most logging,
# some startup messages still go to standard error, so redirect those
# to /dev/null. They still appear in the Tomcat's Toolkit log file.

java -Dcatalina.base=${catalina_base} \
     -cp $TOOLKIT_ROOT/WEB-INF/lib/*:$TOOLKIT_ROOT/WEB-INF/classes \
     au.org.ands.vocabs.toolkit.utils.RedirectCurrent \
     "$@" 2>/dev/null
