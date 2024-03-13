package org.eclipse.tractusx.bdrs.api.management;

import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * Maps a BPN to a DID.
 */
public record BpnMapping(@NotNull String bpn, @NotNull String did) {
    public BpnMapping {
        requireNonNull(bpn, "bpn");
        requireNonNull(did, "did");
    }
}
