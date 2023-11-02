package com.dev.openai.travelassistant.Utils;

import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Service
public class FileUtils {

    public static void saveImages(List<String> imageUrls, String downloadDirectory) throws MalformedURLException {
        for (int i = 0; i < imageUrls.size(); i++) {
            String imageUrl = imageUrls.get(i);
            String fileName = "image_" + (i + 1) + ".jpg";
            URL url = new URL(imageUrl);
            try (InputStream inputStream = url.openStream();
                 OutputStream outputStream = new FileOutputStream(downloadDirectory + fileName)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                System.out.println("Downloaded: " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
