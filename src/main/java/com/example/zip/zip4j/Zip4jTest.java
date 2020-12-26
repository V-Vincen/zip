package com.example.zip.zip4j;

import com.google.common.collect.Lists;
import io.vavr.CheckedConsumer;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.ExcludeFileFilter;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author vincent
 */
public class Zip4jTest {

    @Test
    public void packAddFile() throws ZipException {
        /*
         * 添加具体文件（该文件必须存在，否则会抛异常）到压缩文件中
         */
        //官网案例：Creating a zip file with single file in it / Adding single file to an existing zip
        new ZipFile("dirtest/zip4jtest/filename.zip").addFile("dirtest/zip4jtest/src/a.txt");
        new ZipFile("dirtest/zip4jtest/filename.zip").addFile(new File("dirtest/zip4jtest/src/b.txt"));

        //官网案例：Creating a zip file with multiple files / Adding multiple files to an existing zip
        new ZipFile("dirtest/zip4jtest/filename.zip").addFiles(Arrays.asList(new File("dirtest/zip4jtest/src/first_file"), new File("dirtest/zip4jtest/src/second_file")));
    }

    @Test
    public void packAddFolder() throws ZipException {
        /*
         * 添加具体文件夹（该文件夹必须存在，否则会抛异常）到压缩文件中（被添加的文件夹，可以设置过滤条件）
         */
        //官网案例：Creating a zip file by adding a folder to it / Adding a folder to an existing zip
        new ZipFile("dirtest/zip4jtest/filenamefolder.zip").addFolder(new File("dirtest/zip4jtest/srcdir2"));

        //官网案例：Since v2.6, it is possible to exclude certain files when adding a folder to zip by using an ExcludeFileFilter
        //ExcludeFileFilter：过滤掉文件夹内不需要的文件（该案例是过滤文件名结尾是 ext 格式的文件）
        ExcludeFileFilter excludeFileFilter = file -> FilenameUtils.isExtension(file.getName(), "ext");
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setExcludeFileFilter(excludeFileFilter);

        //CompressionMethod：压缩方式
//        zipParameters.setCompressionMethod(CompressionMethod.STORE);
        new ZipFile("dirtest/zip4jtest/excludefilefilter.zip").addFolder(new File("dirtest/zip4jtest/srcdir3"), zipParameters);
    }

    @Test
    public void packWithPwd() throws ZipException {
        /*
         * 压缩文件并设置保护密码，或者添加文件到要以存在的压缩文件中
         */
        //官网案例：Creating a password protected zip file / Adding files to an existing zip with password protection
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(EncryptionMethod.AES);
        // Below line is optional. AES 256 is used by default. You can override it to use AES 128. AES 192 is supported only for extracting.
        zipParameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);

        List<File> filesToAdd = Arrays.asList(
                new File("dirtest/zip4jtest/src/somefile.txt"),
                new File("dirtest/zip4jtest/src/someotherfile.txt")
        );

        ZipFile zipFile = new ZipFile("dirtest/zip4jtest/filenamepwd.zip", "password".toCharArray());
        zipFile.addFiles(filesToAdd, zipParameters);
    }

    @Test
    public void unpackAllFile() throws ZipException {
        /*
         * 从 zip 提取所有文件
         */
        //官网案例：Extracting all files from a zip
        new ZipFile("dirtest/zip4jtest/filename.zip").extractAll("dirtest/zip4jtest/out");
    }

    @Test
    public void unpackAllFileWithPwd() throws ZipException {
        /*
         * 从受密码保护的 zip 提取所有文件
         */
        //官网案例：Extracting all files from a password protected zip
        new ZipFile("dirtest/zip4jtest/filenamepwd.zip", "password".toCharArray()).extractAll("dirtest/zip4jtest/out");
    }

    @Test
    public void unpackSingleFile() throws ZipException {
        /*
         * 从 zip 提取单个文件
         */
        //官网案例：Extracting a single file from zip
        new ZipFile("dirtest/zip4jtest/filename.zip").extractFile("a.txt", "dirtest/zip4jtest/out");
    }

    @Test
    public void unpackSingleFileWithPwd() throws ZipException {
        /*
         * 从受密码保护的 zip 提取单个文件
         */
        //官网案例：Extracting a single file from zip which is password protected
        new ZipFile("dirtest/zip4jtest/filenamepwd.zip", "password".toCharArray()).extractFile("somefile.txt", "dirtest/zip4jtest/out");
    }

    @Test
    public void zipInputStreamExample() throws Exception {
        /*
         * 使用 ZipInputStream 提取文件（以流的形式解压文件）
         */
        //官网案例：Extract files with ZipInputStream
        String filePath = "dirtest/zip4jtest/zipinputstreamexample.zip";
        String password = "password";
        LocalFileHeader localFileHeader;

        int readLen;
        byte[] readBuffer = new byte[4096];
        InputStream inputStream = new FileInputStream(new File(filePath));
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream, password.toCharArray())) {
            while ((localFileHeader = zipInputStream.getNextEntry()) != null) {
                File extractedFile = new File(localFileHeader.getFileName());
                try (OutputStream outputStream = new FileOutputStream(extractedFile)) {
                    while ((readLen = zipInputStream.read(readBuffer)) != -1) {
                        outputStream.write(readBuffer, 0, readLen);
                    }
                }
            }
        }

        //改动：以流的形式输出
        List<InputStream> list = Lists.newArrayList();
        InputStream newInputStream = Files.newInputStream(Paths.get(filePath));
        try (ZipInputStream zipInputStream = new ZipInputStream(newInputStream, password.toCharArray())) {
            while ((localFileHeader = zipInputStream.getNextEntry()) != null) {
                System.out.println("解压后的文件名：" + localFileHeader.getFileName());
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                IOUtils.copy(zipInputStream, byteArrayOutputStream);
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                list.add(byteArrayInputStream);
            }
        }
        list.forEach(CheckedConsumer.<InputStream>of(
                byteArrayInputStream -> {
                    String str = IOUtils.toString(byteArrayInputStream, StandardCharsets.UTF_8);
                    System.out.println("解压后的文件内容：" + str);
                }).unchecked()
        );
    }


}











