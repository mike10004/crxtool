package com.github.mike10004.crxtool.maven;

import io.github.mike10004.crxtool.CrxPacker;
import io.github.mike10004.crxtool.KeyPairs;
import io.github.mike10004.crxtool.Zipping;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

/**
 * Goal that packs a Chrome extension.
 */
@Mojo(name = "pack-extension", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class PackExtensionMojo extends AbstractMojo {

    public static final String PROP_PREFIX = "crxtool.";

    @Parameter( defaultValue = "${session}", readonly = true )
    private MavenSession session;

    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject project;

    /**
     * Directory that contains extension source code and resource files. This
     * is the parent directory of {@code manifest.json}.
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/extension", property = PROP_PREFIX + "directory")
    private File sourceDirectory;

    /**
     * File containing the private key for extension signing in PEM format.
     */
    @Parameter(property = PROP_PREFIX + "privateKey")
    private File privateKey;

    /**
     * Extension output file pathname. This is the pathname of the CRX file
     * that is to be produced. If you set {@link #excludeHeader} to true, this
     * should be changed to a zip file.
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.name}.crx")
    private File outputFile;

    /**
     * Flag that specifies whether the CRX header should be included. If this is
     * true, the output file is in ZIP format.
     */
    @Parameter
    private boolean excludeHeader;

    @Override
    public void execute() throws MojoExecutionException {
        File outputFile = getOutputFile();
        try {
            KeyPair keyPair;
            if (privateKey != null) {
                byte[] keyBytes;
                try (Reader reader = new InputStreamReader(new FileInputStream(privateKey), StandardCharsets.US_ASCII)) {
                    keyBytes = new PemParser().extractBytes(reader);
                }
                keyPair = KeyPairs.loadRsaKeyPairFromPrivateKeyBytes(keyBytes);
            } else {
                keyPair = KeyPairs.generateRsKeyPair(createRandom());
            }
            Path extensionDir = sourceDirectory.toPath();
            if (isExcludeHeader()) {
                byte[] zipBytes = Zipping.zipDirectory(extensionDir, null);
                java.nio.file.Files.write(outputFile.toPath(), zipBytes);
            } else {
                CrxPacker packer = createPacker();
                try (OutputStream outputStream = new FileOutputStream(outputFile)) {
                    packer.packExtension(extensionDir, keyPair, outputStream);
                }
            }
            getLog().info(String.format("execute: outputFile = %s (%d bytes)", outputFile, outputFile.length()));
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
            throw new MojoExecutionException("mojo execution failed", e);
        }
    }

    protected CrxPacker createPacker() {
        return CrxPacker.getDefault();
    }

    protected SecureRandom createRandom() {
        return new SecureRandom();
    }

    public MavenSession getSession() {
        return session;
    }

    public MavenProject getProject() {
        return project;
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    public File getPrivateKey() {
        return privateKey;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public boolean isExcludeHeader() {
        return excludeHeader;
    }

    void setSession(MavenSession session) {
        this.session = session;
    }

    void setProject(MavenProject project) {
        this.project = project;
    }

    public void setSourceDirectory(File sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    public void setPrivateKey(File privateKey) {
        this.privateKey = privateKey;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public void setExcludeHeader(boolean excludeHeader) {
        this.excludeHeader = excludeHeader;
    }

}
