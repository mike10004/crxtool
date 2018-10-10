package io.github.mike10004.crxtool;

/**
 * @deprecated use {@link Crx2Packer} instead; this class will be removed in a future release
 */
@Deprecated
public final class BasicCrxPacker extends Crx2Packer {

    @Deprecated
    static CrxPacker getDefaultInstance() {
        return Crx2Packer.getDefaultInstance();
    }

}

