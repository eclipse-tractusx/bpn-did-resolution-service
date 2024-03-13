# BPN-DID Resolution Service

The BPN-DID Resolution Service (BDRS) provides a directory of Business Partner Numbers (BPN) and their associated DIDs.
The directory is used by dataspace participant agents to resolve a DID for a BPN.

The directory is requested via a RESTFul HTTPS API and is designed to be cached locally for resolution operations. When
requesting the directory, the client must include a JWT with a presentation containing its `MembershipCrediential` for
authentication.

## Implementation

The BDRS is a collection of extensions to the [EDC core runtime](https://github.com/eclipse-edc/Connector). 
