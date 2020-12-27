package com.example.zip.jdk_zip;

import io.vavr.CheckedConsumer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author vincent
 */
public class ZipTest {
    @Test
    public void zipFile() throws Exception {
        //压缩文件
        String path = "dirtest/jdkziptest";
        Path dirPath = Paths.get(path);
        if (Files.notExists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        String zipFileName = "test.zip";
        Path absoluteZipPath = dirPath.resolve(zipFileName);
        //最终输出的文件路径：/Users/vincent/IDEA_Project/my_project/zip/dirtest/jdkziptest/test.zip
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(absoluteZipPath.toFile()));
        ZipEntry e = new ZipEntry("mytext.txt");
        out.putNextEntry(e);

        StringBuilder sb = new StringBuilder();
        sb.append("Test String");
        byte[] data = sb.toString().getBytes();
        out.write(data, 0, data.length);
        out.closeEntry();

        out.close();
    }

    @Test
    public void zipDir() throws IOException {
        //压缩目录：/Users/vincent/IDEA_Project/my_project/zip/dirtest/jdkziptest/src（中有两个文件：abc.txt、abcd.txt）
        String dirPath = "dirtest/jdkziptest/src";
        Path sourceDir = Paths.get(dirPath);

        //创建目标目录：/Users/vincent/IDEA_Project/my_project/zip/dirtest/jdkziptest/out
        String targetPath = "dirtest/jdkziptest/out";
        Path targetDir = Paths.get(targetPath);
        if (Files.notExists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        //压缩后的文件路径（也是最终压缩后输出的文件路径）：/Users/vincent/IDEA_Project/my_project/zip/dirtest/jdkziptest/out/dirzip.zip
        Path targetZipPath = targetDir.resolve(sourceDir.getFileName().toString().concat(".zip"));
        ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(targetZipPath.toFile()));

        //递归压缩 dirzip 目录下的全部文件，压缩后的文件名为 dirzip.zip，输出到 dirzipout 目录下（中有一个文件：dirzip.zip）。
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                try {
                    Path targetFile = sourceDir.relativize(file);
                    outputStream.putNextEntry(new ZipEntry(targetFile.toString()));
                    byte[] bytes = Files.readAllBytes(file);
                    outputStream.write(bytes, 0, bytes.length);
                    outputStream.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return FileVisitResult.CONTINUE;
            }
        });
        outputStream.close();
    }

    @Test
    public void unzip() throws IOException {
        String dirPath = "dirtest/jdkziptest/out";
        Path filePath = Paths.get(dirPath);

        List<Path> zipPaths = Files.walk(filePath)
                .filter(p -> FilenameUtils.isExtension(p.toFile().getName(), "zip"))
                .collect(Collectors.toList());

        //解压缩 dirzipout 目录下（中有一个压缩文件：dirzip.zip）的全部 .zip 文件，到当前目录（dirzipout）下。最后解压缩出的结果为两个文件：abc.tet、test.zip
        zipPaths.forEach(CheckedConsumer.<Path>of(
                zipPath -> {
                    File file = zipPath.toFile();
                    ZipFile zipFile = new ZipFile(file);
                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while (entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();
                        InputStream inputStream = zipFile.getInputStream(entry);
                        IOUtils.copy(inputStream, new FileOutputStream(filePath.resolve(entry.getName()).toFile()));
                    }
                }).unchecked()
        );
    }
}
