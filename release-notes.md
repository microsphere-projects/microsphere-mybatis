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

**Full Changelog**: https://github.com/microsphere-projects/microsphere-mybatis/compare/0.2.5...0.2.6