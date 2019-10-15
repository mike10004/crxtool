package io.github.mike10004.crxtool;

import com.google.common.base.MoreObjects;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Metadata implementation that is buffered in memory.
 */
class BufferedCrxMetadata implements CrxMetadata {

    private final String id;

    private final String magicNumber;

    private final CrxVersion crxVersion;

    private final CrxFileHeader fileHeader;

    public BufferedCrxMetadata(String magicNumber, CrxVersion crxVersion, CrxFileHeader fileHeader, String id) {
        this.magicNumber = requireNonNull(magicNumber);
        this.crxVersion = requireNonNull(crxVersion);
        this.fileHeader = requireNonNull(fileHeader);
        this.id = requireNonNull(id);
    }

    @Override
    public CrxFileHeader getFileHeader() {
        return fileHeader;
    }

    /**
     * Extension ID, as computed from the public key.
     * @return the id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Magic number of the file.
     * @return the magic number
     */
    @Override
    public String getMagicNumber() {
        return magicNumber;
    }

    /**
     * Version of the CRX file format used.
     * @return the version
     */
    @Override
    public CrxVersion getCrxVersion() {
        return crxVersion;
    }

    @Override
    public String toString() {
        MoreObjects.ToStringHelper h = MoreObjects.toStringHelper(this);
        if (id != null) h.add("id", id);
        if (magicNumber != null) h.add("magicNumber", magicNumber);
        h.add("version", getCrxVersion());
        if (fileHeader != null) h.add("fileHeader", fileHeader);
        return h.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BufferedCrxMetadata)) return false;
        BufferedCrxMetadata that = (BufferedCrxMetadata) o;
        return Objects.equals(crxVersion, that.crxVersion) &&
                Objects.equals(id, that.id) &&
                Objects.equals(magicNumber, that.magicNumber) &&
                Objects.equals(fileHeader, that.fileHeader);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, magicNumber, crxVersion.identifier(), fileHeader);
    }
}
