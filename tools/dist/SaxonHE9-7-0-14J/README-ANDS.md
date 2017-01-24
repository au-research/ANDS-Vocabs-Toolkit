# ANDS Vocabs Toolkit redistribution of Saxon HE

This directory contains a copy of the binary distribution of Saxon HE.
It is included here under the terms of the MPL.
(See `notices/LICENSE.txt`.)

This binary distribution, and its source code, can be downloaded from:
https://sourceforge.net/projects/saxon/files/Saxon-HE/9.7/.

The file `readme97.txt` included here was downloaded separately from
the above URL.

## Clarification about how the ANDS Vocabs Toolkit uses Saxon

Saxon is used only as a part of the build process, to generate a part
of the Toolkit source code.  Please see the Ant task
`generate-registry-db-entity-dao-source` in the top-level `build.xml`.

The Toolkit source code itself does not refer to Saxon in any way, and
Saxon is not required to run the Toolkit.
