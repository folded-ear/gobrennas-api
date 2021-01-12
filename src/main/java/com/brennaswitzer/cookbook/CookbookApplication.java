package com.brennaswitzer.cookbook;

import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.amazonaws.services.textract.model.Document;
import com.amazonaws.util.IOUtils;
import com.brennaswitzer.cookbook.config.AWSProperties;
import com.brennaswitzer.cookbook.config.AppProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableConfigurationProperties({AppProperties.class, AWSProperties.class})
public class CookbookApplication {

    public static void main(String[] args) {
        SpringApplication.run(CookbookApplication.class, args);
    }

    @Bean
    @Profile({"production", "development"})
    public CommandLineRunner awsTextractThing(ResourceLoader resourceLoader, AmazonTextract client) {
        return args -> {
            File rootDir = new File(".").getCanonicalFile();
            File image = new File(rootDir, "pork_chops.jpg");
            File textract = new File(rootDir, "pork_chops_textract.bin");
            DetectDocumentTextResult result;
            if (textract.exists()) {
                System.out.println("Cached textract found; loading...");
                try (FileInputStream fis = new FileInputStream(textract)) {
                    try (BufferedInputStream bis = new BufferedInputStream(fis)) {
                        try (ObjectInputStream ois = new ObjectInputStream(bis)) {
                            result = (DetectDocumentTextResult) ois.readObject();
                        }
                    }
                }
            } else if (image.exists()) {
                System.out.println("No cached textract found: textracting...");
                ByteBuffer imageBytes;
                try (InputStream inputStream = new FileInputStream(image)) {
                    imageBytes = ByteBuffer.wrap(IOUtils.toByteArray(inputStream));
                }
                DetectDocumentTextRequest request = new DetectDocumentTextRequest()
                        .withDocument(new Document()
                                .withBytes(imageBytes));

                result = client.detectDocumentText(request);
                try (FileOutputStream fos = new FileOutputStream(textract)) {
                    try (BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                            oos.writeObject(result);
                        }
                    }
                }
            } else {
                throw new FileNotFoundException("The '" + image + "' file was not found");
            }
            System.out.println();
            System.out.println();
            System.out.println("- Amazon -------------------------------------------------------------");
            System.out.println(result.getBlocks().stream()
                    .filter(b -> "LINE".equals(b.getBlockType()))
                    .map(Block::getText).collect(Collectors.joining("\n")));
            System.out.println("-/Amazon -------------------------------------------------------------");
            System.out.println();
            System.out.println();
            System.exit(0);
        };
    }

}
