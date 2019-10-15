package io.github.mike10004.crxtool;

public enum CrxVersion {

    /**
     * Constant that represents CRX2.
     */
    CRX2(2),

    /**
     * Constant that represents CRX3.
     */
    CRX3(3);

    private final int formatVersion;

    CrxVersion(int formatVersion) {
        this.formatVersion = formatVersion;
    }

    static CrxVersion fromIdentifier(int identifier) {
        for (CrxVersion version : CrxVersion.values()) {
            if (identifier == version.identifier()) {
                return version;
            }
        }
        throw new IllegalArgumentException("unrecognized identifier: " + identifier);
    }

    public int identifier() {
        return formatVersion;
    }

}
