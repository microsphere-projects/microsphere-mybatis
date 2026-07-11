# Release Notes

## v0.2.3

# Release Notes - Version 0.2.3

## New Features
- Add `sync-branches-from-upstream` workflow to streamline branch synchronization. ([7e11743](https://example.com/commit/7e11743))
- Introduce Copilot-based release notes generation. ([fc3490d](https://example.com/commit/fc3490d))

## Bug Fixes
- Fix indentation issue in `maven-publish` workflow. ([cac5712](https://example.com/commit/cac5712))
- Update `MyBatis` version profiles and dependencies for compatibility. ([dee526a](https://example.com/commit/dee526a))

## Other Changes
- Bumped parent POM to version `0.2.10`. ([71e4bc6](https://example.com/commit/71e4bc6))
- Migrated Maven Wrapper to script-only version `3.3.4`. ([45ee3fb](https://example.com/commit/45ee3fb))
- Removed `microsphere-mybatis-core` build script. ([624eb41](https://example.com/commit/624eb41))
- Updated branch references in README. ([584fb8d](https://example.com/commit/584fb8d))

---

For more details, view the commit history [here](https://example.com/commit-history).

## v0.2.4

# Release Notes for v0.2.4

## New Features
- Enhanced release notes generation and automated release creation. (#0803d42)

## Bug Fixes
- Fixed indentation issue in Dependabot configuration. (#f176c1c)

## Documentation
- Updated branch version references in `README`. (#7091371)
- Removed project-specific name from docstring to improve clarity. (#23503e5)

## Dependency Updates
- Upgraded `microsphere-spring-cloud` to v0.2.11. (#2d5d9c4)

## Build and Workflow Enhancements
- Merged main and release branches to maintain consistency. (#a8b5abf, #df70148, #5bdb41c)
- Auto-bumped version to next patch post release of v0.2.3. (#e2d468c)

**Full Changelog**: https://github.com/microsphere-projects/microsphere-mybatis/compare/0.2.3...0.2.4## v0.2.5

# Release Notes for v0.2.5

## New Features
- **ComponentResolvers** are now loaded via a service loader. ([514d1c5](https://example.com))
- Added support for **MapperResolver** in the refactored `ComponentResolver` API. ([ecf5378](https://example.com))
- Introduced **JUnit5 MyBatis test extension** and utility classes. ([bdc3f31](https://example.com))

## Bug Fixes
- Removed support for parameter-based resolution to simplify the API. ([9ba36c8](https://example.com))

## Test Improvements
- Enhanced test coverage for executor assertions. ([69db13f](https://example.com))
- Updated default environment ID in test setup for better reliability. ([3502f82](https://example.com))

## Build and Workflow Enhancements
- Added **Maven server credentials** configuration to the workflow. ([0336245](https://example.com))
- Updated **Maven GitHub Actions workflows** for improved CI/CD. ([488c611](https://example.com))

## Other Changes
- Various internal **refactoring** and adjustments to improve project structure. 
- **Chore commits** for merging branches and version bumps.

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-mybatis/compare/0.2.4...0.2.5## v0.2.6

# Release Notes for Version 0.2.6

## New Features
- **JavaDoc Updates**: Added detailed JavaDoc, including example usage, to all main source files and non-private methods. ([#53](https://github.com/microsphere-projects/pull/53))

## Dependency Updates
- Upgraded **org.mybatis:mybatis-spring** from `3.0.5` to `4.0.0`. ([#48](https://github.com/microsphere-projects/pull/48))
- Bumped **microsphere-spring-cloud** to `0.2.12`.

## Documentation
- Updated README to reflect the latest branch names and version numbers.

## Other Changes
- Replaced `jspecify` annotations with `io.microsphere.Nullable` for consistency.

---

Note: This release includes no functional code changes outside documentation and dependency updates.

**Full Changelog**: https://github.com/microsphere-projects/microsphere-mybatis/compare/0.2.5...0.2.6## v0.2.7

# Release Notes - Version 0.2.7

## New Features
- **BeanSource Support**: Added support for executor discovery via `BeanSource`. [#1cca2c0]

## Bug Fixes
- Fixed Javadoc formatting issues and re-indented the `Child` class. [#13979ac]
- Removed duplicated line separators and trailing whitespace. [#71a01df]

## Documentation
- Removed outdated `zread` and `isitmaintained` badges from the README. [#b599392]

## Dependency Updates
- Upgraded Microsphere Spring Cloud version to `0.2.14`. [#7849eeb]

## Other Changes
- Bumped branch version references in README. [#92f9104]
- Prepared version bump after publishing 0.2.6. [#26bf321]

**Full Changelog**: https://github.com/microsphere-projects/microsphere-mybatis/compare/0.2.6...0.2.7## v0.2.8

## Release Notes - Version 0.2.8

### New Features
- **Bean Registration**: Introduced `registerBeans` for improved bean management. ([#6ddebad](https://example.com))

### Dependency Updates
- Upgraded Microsphere Spring Cloud to version **0.2.15**. ([#b459715](https://example.com))

### Other Changes
- Internal version bumps and merges to maintain release alignment. ([#c0b248f](https://example.com), [#1cb846f](https://example.com), [#2cb4549](https://example.com), [#07c6b45](https://example.com))

**Full Changelog**: https://github.com/microsphere-projects/microsphere-mybatis/compare/0.2.7...0.2.8## v0.2.9

# Release Notes for Version 0.2.9

## New Features
- Simplified bean registration helpers for easier integration. (#20476f0)
- Removed the SPI bean registration method to streamline functionality. (#d55becf)

## Documentation
- Updated README with the latest versions for better clarity. (#a8d541b)

## Dependency Updates
- Bumped parent dependency to version `0.2.16`. (#a8d541b)

## Build and Workflow Enhancements
- Regular merges between `main` and `release` branches to ensure synchronization. (e.g., #2b2a248, #b3b3234, #546138c, #f7e90b3)

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-mybatis/compare/0.2.8...0.2.9## v0.2.10

# Release Notes - Version 0.2.10

## ✨ New Features
- **MyBatis Support**: 
  - Added `EnableMyBatisExtension` and implemented a test interceptor. [#2fe8eef](https://github.com/commit/2fe8eef)  
  - Introduced `ConditionalOnMyBatisAvailable` for enhanced conditional configuration. [#455ec73](https://github.com/commit/455ec73)  
  - Refactored registrars to support MyBatis extensions. [#f1284c3](https://github.com/commit/f1284c3)

## 📄 Documentation
- Updated branch version numbers in the `README`. [#9110a26](https://github.com/commit/9110a26)

## 🔧 Build and Workflow Enhancements
- Performed merges between main and release branches. [#5975fb5](https://github.com/commit/5975fb5), [#1f9fb3c](https://github.com/commit/1f9fb3c), [#7e0594b](https://github.com/commit/7e0594b), [#b32b47e](https://github.com/commit/b32b47e)
- Bumped version to prepare for development after `0.2.9`. [#049b808](https://github.com/commit/049b808)

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-mybatis/compare/0.2.9...0.2.10## v0.2.11

# Release Notes for Version 0.2.11

## Dependency Updates
- Upgraded `microsphere-spring-cloud` to version **0.2.21**. (#271fe2b)

## Code Improvements
- Refactored MyBatis registrar annotation handling to enhance maintainability. (#b358f93)
- Removed unused imports in MyBatis configuration. (#c0d1cf4)

## Documentation
- Updated `README` to reflect branch version number changes. (#699d2e5)

## Other Changes
- Miscellaneous version bumps and branch merges. [skip ci]

**Full Changelog**: https://github.com/microsphere-projects/microsphere-mybatis/compare/0.2.10...0.2.11## v0.2.12

# Release Notes: v0.2.12

## Bug Fixes
- Fixed feature condition mapping to ensure accurate behavior. ([6f786e0](#))

## Documentation
- Updated README for clarity and accuracy. ([b2dfc23](#))

## Other Changes
- Version bumped post v0.2.11 release. ([74935c4](#))
- Merged main and release branches for synchronization. ([236d822](#), [0d4da4d](#), [17bc8b7](#)) 

---

**Full Changelog**: https://github.com/microsphere-projects/microsphere-mybatis/compare/0.2.11...0.2.12## v0.2.13

# Release Notes for Version 0.2.13

## New Features
- **Optional Dependency Enhancements**:  
  - Added optional annotation processor dependency.  
  - Introduced optional dependencies for `spring-test` module.  

## Bug Fixes
- **MyBatis Improvements**:  
  - Refined MyBatis cloud auto-configuration tests.  
  - Hardened availability condition checks for MyBatis.  
  - Improved condition annotations for MyBatis.  

## Test Improvements
- Renamed "auto-config test" to "integration test" for better clarity.  
- Enhanced alignment and structure in MyBatis-related test cases.  

## Dependency Updates
- Updated Microsphere Spring Cloud version to `0.2.23`.  
- Added `cglib` version property to parent POM for consistency.  
- Refined and reordered module dependencies across core, Spring Boot, Spring Cloud, and Microsphere utilities.

## Build and Workflow Enhancements
- Reordered and refined spring module dependency declarations to improve maintainability.  
- Minor adjustments in build configuration and dependencies for better project structure.  

## Documentation
- Updated `README` to reflect branch version matrix accurately.  

---  

**Full Changelog**: https://github.com/microsphere-projects/microsphere-mybatis/compare/0.2.12...0.2.13