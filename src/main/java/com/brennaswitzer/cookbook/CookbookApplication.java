package com.brennaswitzer.cookbook;

import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.model.*;
import com.amazonaws.util.IOUtils;
import com.brennaswitzer.cookbook.config.AWSProperties;
import com.brennaswitzer.cookbook.config.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableConfigurationProperties({AppProperties.class, AWSProperties.class})
public class CookbookApplication {

    public static void main(String[] args) {
        SpringApplication.run(CookbookApplication.class, args);
    }

//    @Bean
    @Profile({"production", "development"})
    public CommandLineRunner awsTextractThing(ObjectMapper objectMapper, AmazonTextract client) {
        return args -> {
            File rootDir = new File(".").getCanonicalFile();
            File image = new File(rootDir, "pork_chops_lg.jpg");
            File textract = new File(rootDir, image.getName().replace(".jpg", "_textract.bin"));
            File json = new File(rootDir, image.getName().replace(".jpg", "_textract.json"));
            File text = new File(rootDir, image.getName().replace(".jpg", ".txt"));
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
//                                .withS3Object(new S3Object()
//                                .withBucket("foodingerdev")
//                                .withName("recipe/15/pork_chops.jpg")));

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

            List<Block> lines = result.getBlocks().stream()
                    .filter(b -> "LINE".equals(b.getBlockType()))
                    .collect(Collectors.toList());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(json,
                    lines.stream()
                            .map(BareLine::fromBlock)
                            .collect(Collectors.toList())
            );

            System.out.println();
            System.out.println();
            System.out.println("- Amazon -------------------------------------------------------------");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(text))) {
                for (Block b : lines) {
                    bw.write(b.getText());
                    bw.newLine();
                    System.out.println(b.getText());
                }
            }
            System.out.println("-/Amazon -------------------------------------------------------------");
            System.out.println();
            System.out.println();
            System.exit(0);
        };
    }

    @lombok.Value
    private static class BareLine {

        public static BareLine fromBlock(Block block) {
            return new BareLine(block.getText(), block.getGeometry().getBoundingBox());
        }

        String text;
        BoundingBox box;

    }

}
