package com.example.zip.jdk_zip.utils;

import io.vavr.CheckedConsumer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author vincent
 */
public class ZipUtils {
    private static final String EXTENSION = ".zip";

    /**
     * 压缩文件
     *
     * @param filePath 需要压缩的文件路径
     * @throws IOException IO异常
     */
    public static void packFile(String filePath) throws IOException {
        Objects.requireNonNull(filePath, "filePath");
        if (Files.notExists(Paths.get(filePath))) {
            throw new NoSuchFileException(filePath);
        }
        if (!Paths.get(filePath).toFile().isFile()) {
            throw new NoSuchFileException(filePath + " is not a file...");
        }
        packFile(filePath, Paths.get(filePath).getParent().toString());
    }

    /**
     * 压缩文件到指定目录
     *
     * @param filePath 需要压缩的文件路径
     * @param desDirPath  压缩到指定文件目录（该目录必须存在，否则抛出异常）
     * @throws IOException IO异常
     */
    public static void packFile(String filePath, String desDirPath) throws IOException {
        /*
         *  假设：filePath -> /Users/vincent/IDEA_Project/my_project/zip/dirtest/jdkziptest/srcfile.txt
         *       desDirPath -> /Users/vincent/IDEA_Project/my_project/zip/dirtest/jdkziptest/out
         */
        Objects.requireNonNull(filePath, "filePath");
        Objects.requireNonNull(desDirPath, "dirPath");

        //获取文件路径：sourceFilePath -> /Users/vincent/IDEA_Project/my_project/zip/dirtest/jdkziptest/srcfile.txt
        Path sourceFilePath = Paths.get(filePath);
        if (Files.notExists(sourceFilePath)) {
            throw new NoSuchFileException(filePath);
        }
        if (!sourceFilePath.toFile().isFile()) {
            throw new NoSuchFileException(filePath + " is not a file...");
        }

        //获取文件目录路径：dirPath -> /Users/vincent/IDEA_Project/my_project/zip/dirtest/jdkziptest/out
        Path sourceDirPath = Paths.get(desDirPath);
        if (Files.notExists(sourceDirPath)) {
            throw new NotDirectoryException(desDirPath);
        }
        if (!Files.isDirectory(sourceDirPath)) {
            throw new NotDirectoryException(desDirPath + " is not a directory...");
        }

        //拼接文件被压缩后的压缩文件名：zipFileName -> "abc" + ".zip" = "abc.zip"
        String zipFileName = FilenameUtils.getBaseName(sourceFilePath.getFileName().toString()).concat(EXTENSION);
        //创建压缩输出流（就是创建最终输出的压缩文件容器），文件被压缩后的压缩文件的全路径（/Users/vincent/IDEA_Project/my_project/zip/dirtest/jdkziptest/out/abc.zip）
        ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(sourceDirPath.resolve(zipFileName).toFile()));
        //创建压缩文件 zip 实例
        ZipEntry zipEntry = new ZipEntry(sourceFilePath.getFileName().toString());
        //开始写入新的 zip 文件条目，并将流定位到条目数据的开头
        outputStream.putNextEntry(zipEntry);
        //读取原文件 filePath
        byte[] bytes = Files.readAllBytes(sourceFilePath);
        //把原文件 filePath 写入到压缩文件中
        outputStream.write(bytes, 0, bytes.length);
        outputStream.closeEntry();
        outputStream.close();
    }

    /**
     * 压缩整个文件目录
     *
     * @param dirPath 需要压缩的文件目录路径
     * @throws IOException IO异常
     */
    public static void packDir(String dirPath) throws IOException {
        Path sourceDir = Paths.get(dirPath);
        if (Files.notExists(sourceDir)) {
            throw new NotDirectoryException(dirPath);
        }
        if (!Files.isDirectory(sourceDir)) {
            throw new NotDirectoryException(dirPath + " is not a directory...");
        }

        ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(sourceDir.toString().concat(EXTENSION)));
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                Path targetFilePath = sourceDir.relativize(filePath);
                outputStream.putNextEntry(new ZipEntry(targetFilePath.toString()));
                byte[] bytes = Files.readAllBytes(filePath);
                outputStream.write(bytes, 0, bytes.length);
                outputStream.closeEntry();
                return super.visitFile(filePath, attrs);
            }
        });
        outputStream.close();
    }


    /**
     * 解压缩目录中的 zip 文件
     *
     * @param dirPath 解压缩该目录下的所以 zip 文件
     * @throws IOException IO异常
     */
    public static void unpackDir(String dirPath) throws IOException {
        Path sourceDirPath = Paths.get(dirPath);
        if (Files.notExists(sourceDirPath)) {
            throw new NotDirectoryException(dirPath);
        }
        if (!Files.isDirectory(sourceDirPath)) {
            throw new NotDirectoryException(dirPath + " is not a directory...");
        }

        List<Path> sourceZipPaths = Files.walk(sourceDirPath)
                .filter(p -> FilenameUtils.isExtension(p.toFile().getName(), "zip"))
                .collect(Collectors.toList());
        sourceZipPaths.forEach(CheckedConsumer.<Path>of(
                sourceZipPath -> unpack(sourceZipPath, sourceDirPath)).unchecked()
        );
    }

    /**
     * 解压缩 zip 文件
     *
     * @param filePath 需要解压缩的文件路径
     * @throws IOException IO异常
     */
    public static void unpackFile(String filePath) throws IOException {
        Path sourceFilePath = Paths.get(filePath);
        if (Files.notExists(sourceFilePath)) {
            throw new NoSuchFileException(filePath);
        }
        if (!sourceFilePath.toFile().isFile()) {
            throw new NoSuchFileException(filePath + " is not a file...");
        }
        unpack(sourceFilePath, sourceFilePath.getParent());
    }

    private static void unpack(Path path, Path parentPath) throws IOException {
        Objects.requireNonNull(path);
        ZipFile zipFile = new ZipFile(path.toFile());
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            InputStream inputStream = zipFile.getInputStream(zipEntry);
            IOUtils.copy(inputStream, new FileOutputStream(parentPath.resolve(zipEntry.getName()).toFile()));
        }
    }
}
