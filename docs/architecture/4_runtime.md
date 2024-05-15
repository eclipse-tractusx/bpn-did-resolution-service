# BDRS at runtime

## Accessing the Directory API

This public-facing API is accessed by clients to obtain a list BPN-to-DID mappings, which they use to resolve the DID
for a particular BPN,
see [API documentation](https://eclipse-tractusx.github.io/bpn-did-resolution-service/openapi/directory-api/).
It is protected with a bearer token in JWT format that has to be attached to the `Authorization` header of the request.

The bearer token must be a VerifiablePresentation in JWT format, that contains the
client's [MembershipCredential](https://github.com/eclipse-tractusx/tractusx-profiles/blob/main/cx/credentials/schema/credentials/membership.credential.schema.json),
typically also in JWT format. The VerifiablePresentation must be signed with the client's private key and contain in
its `kid` header a URI referencing the client's public key, e.g. using a DID: `"did:web:client1#key-1"`.

The VerifiablePresentation JWT must also have the following properties:

- `iss` claim must be identical to the `holder` property
- `kid` header contains the `iss`, e.g. `kid: did:web:client1#key-1` and `iss: did:web:client1`
- `holder` value must be identical to the MembershipCredential's `credentialSubject.id` value

Upon successful evaluation, the BDRS Directory API response with a GZipped binary stream that contains a JSON array
containing BPN - DID mappings similar to:

```json
[
  {
    "bpn": "BPN123",
    "did": "did:web:localhost:member1"
  },
  {
    "bpn": "BPN789",
    "did": "did:web:anotherhost:member2"
  }
]
```

## Client implementation considerations

The Directory API does **not** provide any 1:1 lookup. That means it is **not** possible to query explicitly for a
particular BPN. Instead, clients are supposed to query the entire list and cache it locally for an appropriate amount of
time. BPN resolution requests should **always hit the local cache**!

This was done to avoid high request loads on the BDRS server and to further decentralize the system as much as possible
to avoid bottlenecks and single-points-of-failure.

## Accessing the Management API

_The Management API should only b accessed by authorized users/applications - appropriate hardening measures **must** be
employed._

In all other respects please refer to
the [Management API documentation](https://eclipse-tractusx.github.io/bpn-did-resolution-service/openapi/management-api/).