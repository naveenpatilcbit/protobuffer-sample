package org.protobuffsample.demo.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class AdvFileUploadController {

    // Directory to store uploaded chunks
    private static final String UPLOAD_DIR = "uploads";

    // A simple in-memory map: (uniqueId -> AtomicInteger for counting chunks).
    // In production, you'd store this in a database or a cache system like Redis.
    private ConcurrentHashMap<String, AtomicInteger> chunkCounterMap = new ConcurrentHashMap<>();

    // A simple in-memory map: (uniqueId -> totalChunks).
    // This helps us know how many chunks are expected for each file.
    private ConcurrentHashMap<String, Integer> totalChunksMap = new ConcurrentHashMap<>();

   @PostMapping("/upload")
    public String fileUpload(@RequestParam("fileChunk") MultipartFile fileChunk,
                             @RequestParam("chunkIndex") int chunkIndex,
                             @RequestParam("totalChunks") int totalChunks,
                             @RequestParam("uniqueId") String uniqueId,
                             @RequestParam("actualFileName") String actualFileName
   ){
       try {
       totalChunksMap.putIfAbsent(uniqueId, totalChunks);
       Path sessionDir = Paths.get(UPLOAD_DIR, uniqueId);
       if (!Files.exists(sessionDir)) {
               Files.createDirectories(sessionDir);
       }
           String chunkFileName = "chunk_" + chunkIndex;
           Path chunkFilePath = sessionDir.resolve(chunkFileName);
           Files.write(chunkFilePath, fileChunk.getBytes(), StandardOpenOption.CREATE);
           chunkCounterMap.putIfAbsent(uniqueId, new AtomicInteger(0));
           AtomicInteger uploadedChunksCounter = chunkCounterMap.get(uniqueId);
           int currentCount = uploadedChunksCounter.incrementAndGet();

           // 6. Check if this was the last chunk
           if (currentCount == totalChunks) {
               // Once we have all chunks, let's merge them
               mergeChunks(uniqueId, totalChunks,actualFileName);

               // Clean up in-memory maps if desired (optional)
               chunkCounterMap.remove(uniqueId);
               totalChunksMap.remove(uniqueId);

               return "SUCCESS";
               // Return success, but not the final chunk

           }
       } catch (IOException e) {
           throw new RuntimeException(e);
       }
        return "SUCCESS";
    }

    /**
     * Merges all the chunk files for a given session into a single file.
     */
    private void mergeChunks(String uniqueId, int totalChunks,String actualFileName) throws IOException {
        Path uniqueIdDir = Paths.get(UPLOAD_DIR, uniqueId);
        Path mergedFilePath = Paths.get(UPLOAD_DIR, actualFileName);

        // If there's an existing merged file, delete it first (optional)
        if (Files.exists(mergedFilePath)) {
            Files.delete(mergedFilePath);
        }

        // Create an empty file to start writing the merged content
        Files.createFile(mergedFilePath);

        // Read each chunk in ascending order and append to final file
        for (int chunkIndex = 0; chunkIndex < totalChunks; chunkIndex++) {
            Path chunkFilePath = uniqueIdDir.resolve("chunk_" + chunkIndex);
            byte[] chunkBytes = Files.readAllBytes(chunkFilePath);

            // Append chunk
            Files.write(mergedFilePath, chunkBytes, StandardOpenOption.APPEND);
        }

        // Optionally delete the chunk files or the session folder if you don't need them
         cleanUpSessionDirectory(uniqueIdDir);

        System.out.println("Merged file created at: " + mergedFilePath.toAbsolutePath());
    }

    private void cleanUpSessionDirectory(Path sessionDir) throws IOException {
        if (Files.exists(sessionDir)) {
            Files.walk(sessionDir)
                    .sorted((p1, p2) -> p2.compareTo(p1)) // delete children before directory
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }
}
