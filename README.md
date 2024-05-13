# BPN-DID Resolution Service

[![Contributors][contributors-shield]][contributors-url]
[![Stargazers][stars-shield]][stars-url]
[![Apache 2.0 License][license-shield]][license-url]
[![Latest Release][release-shield]][release-url]

The BPN-DID Resolution Service (BDRS) provides a directory of Business Partner Numbers (BPN) and their associated DIDs.
The directory is used by dataspace participant agents to resolve a DID for a BPN.

The directory is requested via a RESTFul HTTPS API and is designed to be cached locally for resolution operations. When
requesting the directory, the client must include a JWT with a presentation containing its `MembershipCrediential` for
authentication.

## Implementation

The BDRS is a collection of extensions to the [EDC core runtime](https://github.com/eclipse-edc/Connector).

## Variants

There are two general variants of BDRS:

1. Production: named "bdrs-server", this distribution utilizes Postgres and Hashicorp Vault for data and secret
   retention. Please take a look at the [helm chart README](charts/bdrs-server/README.md)
2. Testing: named "bdrs-server-memory", this distribution uses all in-memory components to lower the barrier of entry
   and the need for configuration when testing against BDRS

## Interacting with BDRS

BDRS comes with two APIs:

1. [Management API](https://eclipse-tractusx.github.io/bpn-did-resolution-service/openapi/management-api/): used to
   maintain directory listing entries. Should **not** be exposed without additional protection
   to the internet.
2. [Directory API](https://eclipse-tractusx.github.io/bpn-did-resolution-service/openapi/directory-api/): clients can
   obtain the BPN-DID resolution mapping directory as a whole. Clients __should__ implement
   a reasonable strategy to cache the directory locally. Note that a valid VerifiablePresentation in JWT format,
   containing a valid MembershipCredential (also JWT format) must be provided as `Bearer` token in the `Authorization`
   header!

## Run official Helm charts

checkout the [Chart README](charts/bdrs-server/README.md)

## Build and run BDRS from source

- Build sources (`-x test` skips the tests):
   ```shell
   ./gradlew build -x test
   ```
- Run with `java`:
  ```shell
  java -jar <VM-PARAMS> runtimes/bdrs-server/build/libs/bdrs-server.jar
  java -jar <VM-PARAMS> runtimes/bdrs-server/build/libs/bdrs-server-memory.jar
  ```
  Note that configuration parameters have to be supplied as VM parameters or environment variables.

- Run with Helm (recommended, assuming KinD):
  ```shell
  ./gradlew dockerize
  kind load docker-image bdrs-server:latest
  kind load docker-image bdrs-server-memory:latest
  
  helm install bdrs-server charts/bdrs-server \
    --set server.debug.enabled="true" \
    --set server.image.pullPolicy="Never" \
    --set server.image.tag="latest" \
    --set server.image.repository="bdrs-server" \
    -f path/to/your/values.yaml \
    --wait-for-jobs --timeout=120s --dependency-update
  ```

## License

Distributed under the Apache 2.0 License.
See [LICENSE](./LICENSE) for more information.


[contributors-shield]: https://img.shields.io/github/contributors/eclipse-tractusx/bpn-did-resolution-service.svg?style=for-the-badge

[contributors-url]: https://github.com/eclipse-tractusx/bpn-did-resolution-service/graphs/contributors

[stars-shield]: https://img.shields.io/github/stars/eclipse-tractusx/bpn-did-resolution-service.svg?style=for-the-badge

[stars-url]: https://github.com/eclipse-tractusx/bpn-did-resolution-service/stargazers

[license-shield]: https://img.shields.io/github/license/eclipse-tractusx/bpn-did-resolution-service.svg?style=for-the-badge

[license-url]: https://github.com/eclipse-tractusx/bpn-did-resolution-service/blob/main/LICENSE

[release-shield]: https://img.shields.io/github/v/release/eclipse-tractusx/bpn-did-resolution-service.svg?style=for-the-badge

[release-url]: https://github.com/eclipse-tractusx/bpn-did-resolution-service/releases