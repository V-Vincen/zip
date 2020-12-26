package com.example.zip.jdk_zip;

import com.example.zip.jdk_zip.utils.ZipUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @author vincent
 */
public class ZipUtilsTest {

    @Test
    public void packTest() throws IOException {
        String filePath = "/Users/vincent/IDEA_Project/my_project/zip/dirtest/jdkziptest/srcfile.txt";
        String dirPath = "/Users/vincent/IDEA_Project/my_project/zip/dirtest/jdkziptest/out";
        String dirPath2 = "/Users/vincent/IDEA_Project/my_project/zip/dirtest/jdkziptest/src";
        ZipUtils.packFile(filePath);
        ZipUtils.packFile(filePath, dirPath);
        ZipUtils.packDir(dirPath2);
    }

    @Test
    public void unpackTest() throws IOException {
        String filePath = "/Users/vincent/IDEA_Project/my_project/zip/dirtest/jdkziptest/src/test.zip";
        String dirPath = "/Users/vincent/IDEA_Project/my_project/zip/dirtest/jdkziptest";
        ZipUtils.unpackFile(filePath);
        ZipUtils.unpackDir(dirPath);
    }
}
