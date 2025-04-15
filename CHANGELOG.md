# Changelog

# [1.13.0](https://github.com/clarin-eric/fcs-sru-server/releases/tag/SRUServer-1.13.0) - 2025-04-15

- Changes:
  - Strict Java 8 version check by using strict [maven compile release configuration](https://maven.apache.org/plugins/maven-compiler-plugin/examples/set-compiler-release.html), fails when code uses newer features!

- Dependencies:
  - **BREAKING** Bump `javax.servlet` to `javax.servlet-api:4.0.1` (see [issue](https://github.com/clarin-eric/fcs-simple-endpoint/issues/4))

# [1.12.0](https://github.com/clarin-eric/fcs-sru-server/releases/tag/SRUServer-1.12.0) - 2025-04-04

- Dependencies:
  - Bump Github Actions CI script actions versions
  - Bump Maven build plugin versions
  - Bump `org.slf4j` to `2.0.17`

# [1.11.0](https://github.com/clarin-eric/fcs-sru-server/releases/tag/SRUServer-1.11.0) - 2024-12-06

- Changes:
  - **BREAKING** Fix typo in interface `SRUAuthenticationInfo`

- Dependencies:
  - Bump `org.slf4j` to `2.0.16`

# [1.10.0](https://github.com/clarin-eric/fcs-sru-server/releases/tag/SRUServer-1.10.0) - 2024-02-02

- Additions:
  - Add [Github Pages](https://clarin-eric.github.io/fcs-sru-server/) with [JavaDoc](https://clarin-eric.github.io/fcs-sru-server/project-reports.html)
  - Add Changelog document

  * Add _experimental_ support for processing authenticated requests

- Dependencies:
  - Add `maven-release-plugin`
  - Bump Maven build plugin versions
  - Bump `org.slf4j` to `1.7.36`

- General:
  - Cleanup (Typos, Copyright, `pom.xml` infos)


For older changes, see commit history at [https://github.com/clarin-eric/fcs-sru-server/commits/main/](https://github.com/clarin-eric/fcs-sru-server/commits/main/?after=81e945081a02f984c10c58f5d771914fa0635888+0&branch=main&qualified_name=refs%2Fheads%2Fmain)
