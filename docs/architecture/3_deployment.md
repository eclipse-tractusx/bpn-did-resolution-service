# Deployment

## Using the official Helm chart

The official Helm chart should be used, its documentation can be obtained
from [here](https://github.com/eclipse-tractusx/bpn-did-resolution-service/blob/main/charts/bdrs-server/README.md).

_Any other deployment scenarios, e.g. running it as plain Docker image, or as Java process are neither recommended nor
supported!_

## Preconditions

### Postgres Database

If a dedicated Postgres database is used (i.e. `install.postgresql` in the chart config is set to `false`), a DB user
must be created that has privileges to execute DDL and DML statements, as BDRS will perform migrations on boot-up.

The DB username and password must be stored as secrets in Hashicorp vault, see the [vault section](#hashicorp-vault) for
details.

## Necessary Configuration

When deploying BDRS using the official Helm chart, the following configuration values are **mandatory**:

- `server.endpoints.default.[port|path]`: the port and base path for the Observability API. This API is **not** supposed
  to be reachable
  via the internet!
- `server.endpoints.management.[port|path]`: the port and base path for the Management API. This API is **not** supposed
  to be reachable
  via the internet!
- `server.endpoints.directory.[port|path]`: the port and base path for the Directory API. This API is supposed to be
  internet-facing.
- `server.trustedIssuers` this must be a YAML array containing the DIDs of all trusted credential issuers, for example:
  ```yaml
  server:
    trustedIssuers:
      - "did:web:tractusx-issuer1"
      - "did:web:tractusx-issuer2"
  ```
- `server.endpoints.management.authKeyAlias`: the alias under which the API key is stored in the Vault
- `postgresql.jdbcUrl`: the JDBC url including the DB name of the Postgres database
- `vault.hashicorp.url`: the URL where Hashicorp Vault is reachable
- `vault.hashicorp.token`: the token which BDRS uses to authenticate against Hashicorp Vault
- `vault.hashicorp.paths.secret`: the root path in the vault where all [secrets](#hashicorp-vault) are stored.

## The Directory API

The Directory API is supposed to provide the mapping information to dataspace participants. While it provides
application-layer security through a bearer token, deployment engineers should take appropriate measures such as
SSL/TLS termination, load-balancing, request throttling, creating an audit log, monitoring, etc.

The Directory API **must** be accessible from the internet, e.g. through a Kubernetes Ingress or LoadBalancer.

## The Management API

The Management API's purpose is to give administrators a way to add, update, remove mapping entries. It has some basic
security built-in via an API token, but additional layers of security are **absolutely necessary**. These include - but
are not limited to - using an API gateway or similar infrastructure that performs the following tasks:

- authentication/authorization: only privileged users ("Admins") should be allowed to access the Management API
- request tracing: every request that hits the Management API should be logged including the user information
- limiting network access: it may be advisable to restrict access to the Management API on a network level, e.g. with a
  VPN

The API Key of the Management API must be stored in Hashicorp Vault. The alias is configured using the Helm
value `server.endpoints.management.authKeyAlias` (default is `"mgmt-api-key"`). The API key must be stored in the Vault
using the alias, see the [vault section](#hashicorp-vault) for details.

## Hashicorp Vault

If a dedicated Hashicorp Vault is used (i.e. `install.vault` in the chart config is set to `false`), a user with write
permissions must exist for that vault, so that secrets can be created.

BDRS requires the following secrets to be present **before application startup**.

### Management API Key

The API key must be stored in the vault using an alias (defaulting to `"mgmt-api-key"`) that contains a single element

```json
{
  "content": "super-secret-api-key"
}
```

### Postgres credentials

Access credentials for Postgres are stored in the Vault using the following hard-coded secret names:

- `edc.datasource.didentry.user`: must contain a single item
  ```json
  {
    "content": "super-secret-username"
  }
  ```
- `edc.datasource.didentry.password` must contain a single item
  ```json
  {
    "content": "super-secret-password"
  }
  ```
