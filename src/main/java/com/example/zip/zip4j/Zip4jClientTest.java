package com.example.zip.zip4j;

import com.example.zip.zip4j.utils.ZipClient;
import org.junit.jupiter.api.Test;

/**
 * @author vincent
 */
public class Zip4jClientTest {
    @Test
    public void pack() throws Exception {
        ZipClient zipClient = new ZipClient();
        zipClient.pack("dirtest/zip4jtest/pack.txt");
    }

    @Test
    public void packWithPwd() throws Exception {
        ZipClient zipClient = new ZipClient("password");
        zipClient.pack("dirtest/zip4jtest/packwithpwd.txt");
    }

    @Test
    public void unpack() throws Exception {
        ZipClient zipClient = new ZipClient();
        zipClient.unpack("dirtest/zip4jtest/pack.zip", "dirtest/zip4jtest/out");
    }

    @Test
    public void unpackWithPwd() throws Exception {
        ZipClient zipClient = new ZipClient("password");
        zipClient.unpack("dirtest/zip4jtest/packwithpwd.zip", "dirtest/zip4jtest/out");
    }
}
