# bdrs-server

![Version: 0.5.7](https://img.shields.io/badge/Version-0.5.7-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 0.5.7](https://img.shields.io/badge/AppVersion-0.5.7-informational?style=flat-square)

A Helm chart for the Tractus-X BPN-DID Resolution Service

**Homepage:** <https://github.com/eclipse-tractusx/bpn-did-resolution-service/tree/main/charts/bdrs-server>

# Configure the chart

Optionally provide the following configuration entries to your Tractus-X BDRS Server Helm chart, either by directly setting them (`--set`)
or by supplying an additional yaml file:
- `server.endpoints.default.[port|path]`: the port and base path for the Observability API. This API is **not** supposed to be reachable
   via the internet!
- `server.endpoints.management.[port|path]`: the port and base path for the Management API. This API is **not** supposed to be reachable
   via the internet!
- `server.endpoints.directory.[port|path]`: the port and base path for the Directory API. This API is supposed to be internet-facing.

### Launching the application

Simply execute these commands on a shell:

```shell
helm repo add tractusx https://eclipse-tractusx.github.io/charts/dev
helm install my-release tractusx-edc/bdrs-server --version 0.5.7 \
     -f <path-to>/additional-values-file.yaml \
     --wait-for-jobs --timeout=120s --dependency-update
```

## Source Code

* <https://github.com/eclipse-tractusx/bpn-did-resolution-service/tree/main/charts/bdrs-server>

## Requirements

| Repository | Name | Version |
|------------|------|---------|
| https://helm.releases.hashicorp.com | vault(vault) | 0.28.0 |
| oci://registry-1.docker.io/cloudpirates | postgresql(postgres) | 0.11.0 |

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| customCaCerts | object | `{}` | Add custom ca certificates to the truststore |
| customLabels | object | `{}` | To add some custom labels |
| fullnameOverride | string | `""` |  |
| imagePullSecrets | list | `[]` | Existing image pull secret to use to [obtain the container image from private registries](https://kubernetes.io/docs/concepts/containers/images/#using-a-private-registry) |
| install.postgresql | bool | `true` |  |
| install.vault | bool | `true` |  |
| nameOverride | string | `""` |  |
| postgresql.auth.database | string | `"bdrs"` |  |
| postgresql.auth.password | string | `"password"` |  |
| postgresql.auth.username | string | `"bdrs"` |  |
| postgresql.image.registry | string | `"docker.io"` | PostgreSQL image registry |
| postgresql.image.repository | string | `"postgres"` | PostgreSQL image repository |
| postgresql.jdbcUrl | string | `"jdbc:postgresql://{{ .Release.Name }}-postgresql:5432/bdrs"` |  |
| postgresql.persistence.enabled | bool | `false` |  |
| postgresql.persistence.size | string | `"10Gi"` |  |
| postgresql.persistence.storageClass | string | `"standard"` |  |
| postgresql.resources.limits.cpu | int | `1` |  |
| postgresql.resources.limits.memory | string | `"1Gi"` |  |
| postgresql.resources.requests.cpu | string | `"250m"` |  |
| postgresql.resources.requests.memory | string | `"256Mi"` |  |
| server.affinity | object | `{}` |  |
| server.autoscaling.enabled | bool | `false` | Enables [horizontal pod autoscaling](https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/) |
| server.autoscaling.maxReplicas | int | `100` | Maximum replicas if resource consumption exceeds resource threshholds |
| server.autoscaling.minReplicas | int | `1` | Minimal replicas if resource consumption falls below resource threshholds |
| server.autoscaling.targetCPUUtilizationPercentage | int | `80` | targetAverageUtilization of cpu provided to a pod |
| server.autoscaling.targetMemoryUtilizationPercentage | int | `80` | targetAverageUtilization of memory provided to a pod |
| server.debug.enabled | bool | `false` |  |
| server.debug.port | int | `1044` |  |
| server.debug.suspendOnStart | bool | `false` |  |
| server.endpoints | object | `{"default":{"path":"/api","port":8080},"directory":{"path":"/api/directory","port":8082},"management":{"authKeyAlias":"mgmt-api-key","authType":"tokenbased","path":"/api/management","port":8081}}` | endpoints of the control plane |
| server.endpoints.default | object | `{"path":"/api","port":8080}` | default api for health checks, should not be added to any ingress |
| server.endpoints.default.path | string | `"/api"` | path for incoming api calls |
| server.endpoints.default.port | int | `8080` | port for incoming api calls |
| server.endpoints.directory | object | `{"path":"/api/directory","port":8082}` | directory API |
| server.endpoints.directory.path | string | `"/api/directory"` | path for incoming api calls |
| server.endpoints.directory.port | int | `8082` | port for incoming api calls |
| server.endpoints.management | object | `{"authKeyAlias":"mgmt-api-key","authType":"tokenbased","path":"/api/management","port":8081}` | management api, used by internal users, can be added to an ingress and must not be internet facing |
| server.endpoints.management.authKeyAlias | string | `"mgmt-api-key"` | authentication key, must be attached to each 'X-Api-Key' request header |
| server.endpoints.management.authType | string | `"tokenbased"` | Authentication type token based |
| server.endpoints.management.path | string | `"/api/management"` | path for incoming api calls |
| server.endpoints.management.port | int | `8081` | port for incoming api calls |
| server.env | object | `{}` |  |
| server.envConfigMapNames | list | `[]` |  |
| server.envSecretNames | list | `[]` |  |
| server.envValueFrom | object | `{}` |  |
| server.image.pullPolicy | string | `"IfNotPresent"` | [Kubernetes image pull policy](https://kubernetes.io/docs/concepts/containers/images/#image-pull-policy) to use |
| server.image.repository | string | `""` |  |
| server.image.tag | string | `""` | Overrides the image tag whose default is the chart appVersion |
| server.ingresses[0].annotations | object | `{}` | Additional ingress annotations to add |
| server.ingresses[0].certManager.clusterIssuer | string | `""` | If preset enables certificate generation via cert-manager cluster-wide issuer |
| server.ingresses[0].certManager.issuer | string | `""` | If preset enables certificate generation via cert-manager namespace scoped issuer |
| server.ingresses[0].className | string | `""` | Defines the [ingress class](https://kubernetes.io/docs/concepts/services-networking/ingress/#ingress-class)  to use |
| server.ingresses[0].enabled | bool | `false` |  |
| server.ingresses[0].endpoints | list | `["directory"]` | EDC endpoints exposed by this ingress resource |
| server.ingresses[0].hostname | string | `"bdrs-server.directory.local"` | The hostname to be used to precisely map incoming traffic onto the underlying network service |
| server.ingresses[0].tls | object | `{"enabled":false,"secretName":""}` | TLS [tls class](https://kubernetes.io/docs/concepts/services-networking/ingress/#tls) applied to the ingress resource |
| server.ingresses[0].tls.enabled | bool | `false` | Enables TLS on the ingress resource |
| server.ingresses[0].tls.secretName | string | `""` | If present overwrites the default secret name |
| server.ingresses[1].annotations | object | `{}` | Additional ingress annotations to add |
| server.ingresses[1].certManager.clusterIssuer | string | `""` | If preset enables certificate generation via cert-manager cluster-wide issuer |
| server.ingresses[1].certManager.issuer | string | `""` | If preset enables certificate generation via cert-manager namespace scoped issuer |
| server.ingresses[1].className | string | `""` | Defines the [ingress class](https://kubernetes.io/docs/concepts/services-networking/ingress/#ingress-class)  to use |
| server.ingresses[1].enabled | bool | `false` |  |
| server.ingresses[1].endpoints | list | `["management"]` | EDC endpoints exposed by this ingress resource |
| server.ingresses[1].hostname | string | `"bdrs-server.mgmt.local"` | The hostname to be used to precisely map incoming traffic onto the underlying network service |
| server.ingresses[1].tls | object | `{"enabled":false,"secretName":""}` | TLS [tls class](https://kubernetes.io/docs/concepts/services-networking/ingress/#tls) applied to the ingress resource |
| server.ingresses[1].tls.enabled | bool | `false` | Enables TLS on the ingress resource |
| server.ingresses[1].tls.secretName | string | `""` | If present overwrites the default secret name |
| server.initContainers | list | `[]` |  |
| server.livenessProbe.enabled | bool | `true` | Whether to enable kubernetes [liveness-probe](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/) |
| server.livenessProbe.failureThreshold | int | `6` | when a probe fails kubernetes will try 6 times before giving up |
| server.livenessProbe.initialDelaySeconds | int | `5` | seconds to wait before performing the first liveness check |
| server.livenessProbe.periodSeconds | int | `5` | this fields specifies that kubernetes should perform a liveness check every 5 seconds |
| server.livenessProbe.successThreshold | int | `1` | number of consecutive successes for the probe to be considered successful after having failed |
| server.livenessProbe.timeoutSeconds | int | `5` | number of seconds after which the probe times out |
| server.log.level | string | `"INFO"` | Defines the log granularity of the default Console Monitor. |
| server.nodeSelector | object | `{}` |  |
| server.podAnnotations | object | `{}` | additional annotations for the pod |
| server.podLabels | object | `{}` | additional labels for the pod |
| server.podSecurityContext | object | `{"fsGroup":10001,"runAsGroup":10001,"runAsUser":10001,"seccompProfile":{"type":"RuntimeDefault"}}` | The [pod security context](https://kubernetes.io/docs/tasks/configure-pod-container/security-context/#set-the-security-context-for-a-pod) defines privilege and access control settings for a Pod within the deployment |
| server.podSecurityContext.fsGroup | int | `10001` | The owner for volumes and any files created within volumes will belong to this guid |
| server.podSecurityContext.runAsGroup | int | `10001` | Processes within a pod will belong to this guid |
| server.podSecurityContext.runAsUser | int | `10001` | Runs all processes within a pod with a special uid |
| server.podSecurityContext.seccompProfile.type | string | `"RuntimeDefault"` | Restrict a Container's Syscalls with seccomp |
| server.readinessProbe.enabled | bool | `true` | Whether to enable kubernetes [readiness-probes](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/) |
| server.readinessProbe.failureThreshold | int | `6` | when a probe fails kubernetes will try 6 times before giving up |
| server.readinessProbe.initialDelaySeconds | int | `5` | seconds to wait before performing the first readiness check |
| server.readinessProbe.periodSeconds | int | `5` | this fields specifies that kubernetes should perform a readiness check every 5 seconds |
| server.readinessProbe.successThreshold | int | `1` | number of consecutive successes for the probe to be considered successful after having failed |
| server.readinessProbe.timeoutSeconds | int | `5` | number of seconds after which the probe times out |
| server.replicaCount | int | `1` |  |
| server.resources | object | `{"limits":{"cpu":1.5,"memory":"512Mi"},"requests":{"cpu":"500m","memory":"128Mi"}}` | [resource management](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/) for the container |
| server.securityContext.allowPrivilegeEscalation | bool | `false` | Controls [Privilege Escalation](https://kubernetes.io/docs/concepts/security/pod-security-policy/#privilege-escalation) enabling setuid binaries changing the effective user ID |
| server.securityContext.capabilities.add | list | `[]` | Specifies which capabilities to add to issue specialized syscalls |
| server.securityContext.capabilities.drop | list | `["ALL"]` | Specifies which capabilities to drop to reduce syscall attack surface |
| server.securityContext.readOnlyRootFilesystem | bool | `true` | Whether the root filesystem is mounted in read-only mode |
| server.securityContext.runAsNonRoot | bool | `true` | Requires the container to run without root privileges |
| server.securityContext.runAsUser | int | `10001` | The container's process will run with the specified uid |
| server.service.annotations | object | `{}` |  |
| server.service.type | string | `"ClusterIP"` | [Service type](https://kubernetes.io/docs/concepts/services-networking/service/#publishing-services-service-types) to expose the running application on a set of Pods as a network service. |
| server.tolerations | list | `[]` |  |
| server.trustedIssuers | list | `[]` | Configures the trusted issuers for this runtime. Must not be empty. |
| server.url.protocol | string | `""` | Explicitly declared url for reaching the dsp api (e.g. if ingresses not used) |
| server.url.public | string | `""` |  |
| server.url.readiness | string | `""` |  |
| server.volumeMounts | list | `[]` | declare where to mount [volumes](https://kubernetes.io/docs/concepts/storage/volumes/) into the container |
| server.volumes | list | `[]` | [volume](https://kubernetes.io/docs/concepts/storage/volumes/) directories |
| serviceAccount.annotations | object | `{}` |  |
| serviceAccount.create | bool | `true` |  |
| serviceAccount.imagePullSecrets | list | `[]` | Existing image pull secret bound to the service account to use to [obtain the container image from private registries](https://kubernetes.io/docs/concepts/containers/images/#using-a-private-registry) |
| serviceAccount.name | string | `""` |  |
| tests | object | `{"hookDeletePolicy":"before-hook-creation,hook-succeeded"}` | Configurations for Helm tests |
| tests.hookDeletePolicy | string | `"before-hook-creation,hook-succeeded"` | Configure the hook-delete-policy for Helm tests |
| vault.hashicorp.healthCheck.enabled | bool | `true` |  |
| vault.hashicorp.healthCheck.standbyOk | bool | `true` |  |
| vault.hashicorp.paths.health | string | `"/v1/sys/health"` |  |
| vault.hashicorp.paths.secret | string | `"/v1/secret"` |  |
| vault.hashicorp.timeout | int | `30` |  |
| vault.hashicorp.token | string | `"root"` |  |
| vault.hashicorp.url | string | `"http://{{ .Release.Name }}-vault:8200"` |  |
| vault.injector.enabled | bool | `false` |  |
| vault.server.dev.devRootToken | string | `"root"` |  |
| vault.server.dev.enabled | bool | `true` |  |
| vault.server.postStart | string | `nil` |  |

----------------------------------------------
Autogenerated from chart metadata using [helm-docs v1.14.2](https://github.com/norwoodj/helm-docs/releases/v1.14.2)
