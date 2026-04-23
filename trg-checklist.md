## QG checks checklist

The checklist list the fulfillment concerning each TRG point and in which frequency the check needs to be renewed

### TRG 1 Documentation

- [TRG 1.01](https://eclipse-tractusx.github.io/docs/release/trg-1/trg-1-1) appropriate `README.md`
  - Fulfilled, check frequency, if something substantial is changing
- [TRG 1.02](https://eclipse-tractusx.github.io/docs/release/trg-1/trg-1-2) appropriate install instructions
  - Fulfilled as part of README.md, check frequency, only if the whole installation process is redesigned
- [TRG 1.03](https://eclipse-tractusx.github.io/docs/release/trg-1/trg-1-3) appropriate release notes
  - Fulfilled by the release mechanism, check-frequency, every release, the generated release notes must be checked for
    completeness and correctness
- [TRG 1.04](https://eclipse-tractusx.github.io/docs/release/trg-1/trg-1-4) editable static files
  - Fulfilled, check frequency, if documentation is changed
- [TRG 1.05](https://eclipse-tractusx.github.io/docs/release/trg-1/trg-1-05) architecture docs
  - Fulfilled see "docs/architecture", check frequency, none, but architecture documentation should be adopted on
    major changes
- [TRG 1.06](https://eclipse-tractusx.github.io/docs/release/trg-1/trg-1-06) administrator guide
  - Fulfilled as part of README.md, check frequency, only if there are substantial changes regarding the administration
    of the product
- [TRG 1.07](https://eclipse-tractusx.github.io/docs/release/trg-1/trg-1-07) user manual
  - Fulfilled as part of README.md, check frequency, only if there are substantial changes regarding the usage
    of the product
- [TRG 1.08](https://eclipse-tractusx.github.io/docs/release/trg-1/trg-1-08) open api docs
  - Fulfilled by build automation, check frequency, every release, the released open-api version needs to be added to
    the .tractusx file in the root of the repository
- [TRG 1.09](https://eclipse-tractusx.github.io/docs/release/trg-1/trg-1-09) migration information
  - Fulfilled in the "docs/migration" folder, check frequency, every release, a new migration guide needs to be added

#### TRG 2 Git

- [TRG 2.01](https://eclipse-tractusx.github.io/docs/release/trg-2/trg-2-01) default branch is named `main`
  - Fulfilled, check frequency, none 
- [TRG 2.03](https://eclipse-tractusx.github.io/docs/release/trg-2/trg-2-03) repository structure
  - Fulfilled, check frequency, none
- [TRG 2.04](https://eclipse-tractusx.github.io/docs/release/trg-2/trg-2-04) leading product repository
  - Not applicable as the product only has this repository
- [TRG 2.05](https://eclipse-tractusx.github.io/docs/release/trg-2/trg-2-05) `.tractusx` metafile in a proper format
  - Fulfilled, check frequency, none

#### TRG 3 Kubernetes

- [TRG 3.02](https://eclipse-tractusx.github.io/docs/release/trg-3/trg-3-02) persistent volume and persistent volume
  claim or database dependency (subchart) are in place when needed
  - Not actually fulfilled, something for the future, not priority

#### TRG 4 Container

- [TRG 4.01](https://eclipse-tractusx.github.io/docs/release/trg-4/trg-4-01) [semantic versioning](https://semver.org/)
  and tagging <!-- container is tagged correctly additionally to the latest tag -->
  - Fulfilled by release automation, check frequency, none
- [TRG 4.02](https://eclipse-tractusx.github.io/docs/release/trg-4/trg-4-02) base image is agreed
  <!-- Java, Kotlin, ... if JVM based language use base image from [Eclipse Temurin](https://hub.docker.com/_/eclipse-temurin) -->
  - Fulfilled and automatically managed by dependabot, check frequency, none
- [TRG 4.03](https://eclipse-tractusx.github.io/docs/release/trg-4/trg-4-03) image has `USER` command and Non Root Container
  - Fulfilled, check frequency, none 
- [TRG 4.05](https://eclipse-tractusx.github.io/docs/release/trg-4/trg-4-05) released image must be placed in
  `DockerHub`, remove `GHCR` references
  - Fulfilled, check frequency, none
- [TRG 4.06](https://eclipse-tractusx.github.io/docs/release/trg-4/trg-4-06) separate notice file for `DockerHub`
  has all necessary information
  - Fulfilled, check frequency, none
- [TRG 4.07](https://eclipse-tractusx.github.io/docs/release/trg-4/trg-4-07) root file system is set to read access 
  by default, but can be overwritten by the user
  - Not actually fulfilled, something for the future, not priority
- [TRG 4.08](https://eclipse-tractusx.github.io/docs/release/trg-4/trg-4-08) multi-platform images
  - Not Fulfilled, something for the future, not priority

#### TRG 5 Helm

- [TRG 5.01](https://eclipse-tractusx.github.io/docs/release/trg-5/trg-5-01) Helm chart requirements
  - Fulfilled, check frequency, none
- [TRG 5.02](https://eclipse-tractusx.github.io/docs/release/trg-5/trg-5-02) Helm chart location in `/charts` directory
  and correct structure
  - Fulfilled, check frequency, none
- [TRG 5.03](https://eclipse-tractusx.github.io/docs/release/trg-5/trg-5-03) proper version strategy
  - Fulfilled, check frequency, none
- [TRG 5.04](https://eclipse-tractusx.github.io/docs/release/trg-5/trg-5-04) CPU / MEM resource requests and limits and
  are properly set
  - Fulfilled, check frequency, none 
- [TRG 5.06](https://eclipse-tractusx.github.io/docs/release/trg-5/trg-5-06) Application must be configurable through the 
  Helm chart <!-- every startup configuration aspect of your application must be configurable through the Helm chart
  (ingress class, tls, labels, annotations, database, secrets, persistence, env variables) -->
  - Fulfilled, check frequency, none
- [TRG 5.07](https://eclipse-tractusx.github.io/docs/release/trg-5/trg-5-07) Dependencies are present and properly
  configured in the Chart.yaml
  - Fulfilled, check frequency, none
- [TRG 5.08](https://eclipse-tractusx.github.io/docs/release/trg-5/trg-5-08) Product has a single deployable helm chart
  that contains all components <!--(backend, frontend, etc.) -->
  - Fulfilled, check frequency, none
- [TRG 5.09](https://eclipse-tractusx.github.io/docs/release/trg-5/trg-5-09) Helm Test running properly
  - Fulfilled, by having deployment and upgradability tests, not exactly as proposed in the TRG, future topic, no priority,
    check frequency, none
- [TRG 5.10](https://eclipse-tractusx.github.io/docs/release/trg-5/trg-5-10) Products need to support 3 versions at a time
  - Fulfilled, check frequency, every release to update the used versions
- [TRG 5.11](https://eclipse-tractusx.github.io/docs/release/trg-5/trg-5-11) Upgradeability
  - Fulfilled, part of the automated tests, check frequency, none

#### TRG 6 Released Helm Chart

- [TRG 6.01](https://eclipse-tractusx.github.io/docs/release/trg-6/trg-6-01) Released Helm Chart <!-- A released Helm
  chart for each Tractus-X sub-product is expected to be available in corresponding GitHub repository. -->
  - Fulfilled, check frequency, none

#### TRG 7 Open Source Governance
- [TRG 7.01](https://eclipse-tractusx.github.io/docs/release/trg-7/trg-7-01) Legal Documentation
  - Fulfilled, all files exists, check frequency, only when changes are required from the project community
- [TRG 7.02](https://eclipse-tractusx.github.io/docs/release/trg-7/trg-7-02) License and copyright header
  - Fulfilled, part of pr verification, check frequency, none 
- [TRG 7.03](https://eclipse-tractusx.github.io/docs/release/trg-7/trg-7-03) IP checks for project content
  <!-- for each PR containing more than 1000 relevant lines there **must** be an approved
  [IP review for Code Contributions](/docs/oss/issues#eclipse-gitlab-ip-issue-tracker) before the contribution can be
  pushed/merged -->
  - Not applicable as trg, only during processing of PRs
- [TRG 7.04](https://eclipse-tractusx.github.io/docs/release/trg-7/trg-7-04) IP checks for 3rd party content
  - Fulfilled, part of release verification, check frequency, none (for trg)
- [TRG 7.05](https://eclipse-tractusx.github.io/docs/release/trg-7/trg-7-05) Legal information for distributions
  - Fulfilled by release automation, check frequency, on changes concerning TRG 7.1
- [TRG 7.06](https://eclipse-tractusx.github.io/docs/release/trg-7/trg-7-06) Legal information for end user content
  - Not applicable 
- [TRG 7.07](https://eclipse-tractusx.github.io/docs/release/trg-7/trg-7-07) Legal notice for documentation (non-code)
  - Not applicable
- [TRG 7.08](https://eclipse-tractusx.github.io/docs/release/trg-7/trg-7-08) Legal notice for KIT documentation
  - Not applicable

#### TRG 8 Security
- [TRG 8.01](https://eclipse-tractusx.github.io/docs/release/trg-8/trg-8-01) Mitigate high and above findings in CodeQL
  - Fulfilled, part of pr automation, check frequency, every release
- [TRG 8.02](https://eclipse-tractusx.github.io/docs/release/trg-8/trg-8-02) Mitigate high and above findings in KICS
  - Fulfilled, part of pr automation, check frequency, every release
- [TRG 8.03](https://eclipse-tractusx.github.io/docs/release/trg-8/trg-8-03) No secret findings by GitGuardian or TruffleHog
  - Fulfilled, part of pr automation, check frequency, every release
- [TRG 8.04](https://eclipse-tractusx.github.io/docs/release/trg-8/trg-8-04) Mitigate high and above findings in Trivy
  - Fulfilled, part of pr automation, check frequency, every release
- [TRG 8.05](https://eclipse-tractusx.github.io/docs/release/trg-8/trg-8-05) Dependabot
  - Fulfilled, part of project automation, check frequency, none  

#### TRG 9 UX/UI Styleguide
- [TRG 9.01](https://eclipse-tractusx.github.io/docs/release/trg-9/trg-9-01) UI consistency/styleguide for UI
  - Not applicable, as the product does not have a UI
