package com.example.zip.zip4j.utils;

import com.google.common.collect.Lists;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author vincent
 */
public class ZipClient {
    private String password;
    private static final String EXTENSION = "zip";

    public ZipClient() {
    }

    public ZipClient(String password) {
        this.password = password;
    }

    public void pack(List<String> filePaths, String desZipFileName) throws ZipException {
        Objects.requireNonNull(filePaths);
        Objects.requireNonNull(desZipFileName);

        List<File> files = filePaths.stream().map(File::new).collect(Collectors.toList());
        ZipFile zipFile = new ZipFile(desZipFileName + "." + EXTENSION, Optional.ofNullable(password).map(String::toCharArray).orElse(null));
        zipFile.addFiles(files, Optional.ofNullable(password)
                .map(p -> {
                    ZipParameters zipParameters = new ZipParameters();
                    zipParameters.setCompressionMethod(CompressionMethod.DEFLATE);
                    zipParameters.setCompressionLevel(CompressionLevel.ULTRA);
                    zipParameters.setEncryptFiles(true);
                    zipParameters.setEncryptionMethod(EncryptionMethod.AES);
                    zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
                    return zipParameters;
                })
                .orElse(new ZipParameters())
        );
    }

    public void pack(String filePath) throws ZipException, NoSuchFileException {
        Objects.requireNonNull(filePath);
        Path path = Paths.get(filePath);
        if (Files.notExists(path)) {
            throw new NoSuchFileException(filePath);
        }
        if (!path.toFile().isFile()) {
            throw new NoSuchFileException(filePath + " is not a file...");
        }

        this.pack(Lists.newArrayList(filePath), path.getParent().resolve(FilenameUtils.getBaseName(filePath)).toString());
    }

    public void unpack(String sourceZipFilePath, String extractedZipDirPath) throws ZipException, NoSuchFileException, NotDirectoryException {
        Objects.requireNonNull(sourceZipFilePath);
        Objects.requireNonNull(extractedZipDirPath);

        if (Files.notExists(Paths.get(sourceZipFilePath))) {
            throw new NoSuchFileException(sourceZipFilePath);
        }
        if (!Paths.get(sourceZipFilePath).toFile().isFile()) {
            throw new NoSuchFileException(sourceZipFilePath + " is not a file...");
        }

        if (Files.notExists(Paths.get(extractedZipDirPath))) {
            throw new NotDirectoryException(extractedZipDirPath);
        }
        if (!Files.isDirectory(Paths.get(extractedZipDirPath))) {
            throw new NotDirectoryException(extractedZipDirPath + " is not a directory...");
        }

        ZipFile zipFile = new ZipFile(sourceZipFilePath);
        if (zipFile.isEncrypted()) {
            zipFile.setPassword(password.toCharArray());
        }
        zipFile.extractAll(extractedZipDirPath);
    }
}
