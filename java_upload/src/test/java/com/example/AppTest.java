package com.example;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue(true);
    }

    public static void main(String[] args) {
        // String path = "C:\\uploads\\test.mkv";
        // try {
        // MessageDigest md = MessageDigest.getInstance("MD5");
        // FileInputStream fis = new FileInputStream(path);
        // byte[] buffer = new byte[1024];
        // int len;

        // while ((len = fis.read(buffer)) != -1) {
        // md.update(buffer, 0, len);
        // }
        // fis.close();
        // byte[] digest = md.digest();
        // StringBuilder str = new StringBuilder();
        // for (byte b : digest) {
        // String hex = String.format("%02x", b);
        // str.append(hex);
        // }

        // System.out.println(str);// 8dd05d3d7d40ac3829e5e85f8158fe69
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        
    }
}
