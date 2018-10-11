Changelog
=========

0.12
----

* breaking change: make `CrxMetadata `an interface instead of a class
* breaking change: remove `BasicCrxPacker`; use `Crx2Packer` instead
* exclude *crxtool-testing* artifact from deployment 

0.11
----

* deprecate `CrxParser.CrxParsingException` in favor of top-level exception class
* move `PemParser` to core module
* fix typo in `KeyPairs.generateRsaKeyPair`
* remove stray printing to stdout when parsing CRX3 files

0.10
----

* support parsing CRX3

0.9
---

* report magic number errors with more transparency

0.7
---

* upgrade dependencies

0.6
---

* support key generation even if key file is absent

0.4
---

* refactor into core and Maven plugin projects

0.3
---

* support packing CRX files

0.1
---

* initial release: support parsing CRX files
