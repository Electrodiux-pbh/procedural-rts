package com.electrodiux.main;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LibrariesInstaller {

    public static final String REPO = "Electrodiux-pbh/procedural-rts";
    public static final String REPO_FOLDER = "libraries";

    public static final String FOLDER_API_URL = "https://api.github.com/repos/" + REPO + "/contents/" + REPO_FOLDER;
    public static final String RAW_FOLDER_API_URL = "https://raw.githubusercontent.com/" + REPO + "/master/"
            + REPO_FOLDER + "/";

    public static final String DESTINATION_FOLDER = "libraries";
    public static final String MAIN_CLASS = "com.electrodiux.main.Main";
    public static final String JAR_FILE = "procedural-rts.jar";

    public static void main(String[] args) throws IOException {
        // Downloading file list from the GitHub API
        String fileList = new String(downloadFromUrl(FOLDER_API_URL));
        String[] fileNames = fileList.split("\"name\":");

        // Download each file
        for (int i = 1; i < fileNames.length; i++) {
            String fileName = fileNames[i].substring(1, fileNames[i].indexOf('"', 1));
            String fileUrl = RAW_FOLDER_API_URL + fileName;
            System.out.println("Downloading: '" + fileName + "' from: " + fileUrl);
            try {
                downloadFile(fileUrl, DESTINATION_FOLDER + "/" + fileName);
            } catch (IOException e) {
                System.err.println("An error occurred while downloading file '" + fileName + "'' from: " + fileUrl
                        + " | Caused by: " + e.getMessage());
            }
        }

        System.out.println("Creating run.bat file...");

        String batContent = "java -cp \"" + DESTINATION_FOLDER + "/*;" + JAR_FILE + "\" " + MAIN_CLASS + "\nPAUSE";
        Files.write(Paths.get("run.bat"), batContent.getBytes());

        System.out.println("Installation complete");
    }

    private static byte[] downloadFromUrl(String url) throws IOException {
        URL u = new URL(url);
        BufferedInputStream in = new BufferedInputStream(u.openStream());
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();
        in.close();
        return out.toByteArray();
    }

    private static void downloadFile(String url, String destPath) throws IOException {
        URL fileUrl = new URL(url);

        File destFile = new File(destPath);

        destFile.getParentFile().mkdirs();

        try (BufferedInputStream in = new BufferedInputStream(fileUrl.openStream());
                FileOutputStream fileOutputStream = new FileOutputStream(destFile)) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        }
    }
}
