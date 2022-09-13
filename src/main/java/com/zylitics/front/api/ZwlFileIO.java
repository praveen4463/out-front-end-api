package com.zylitics.front.api;

import com.zylitics.front.model.ProjectDownloadableFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZwlFileIO {
  
  private static final String SYS_DEF_TEMP_DIR = System.getProperty("java.io.tmpdir");
  
  private Map<String, String> convertToZwlFile(List<ProjectDownloadableFile> files) {
    Map<String, String> fileNameToText = new HashMap<>();
  
    files.forEach(f -> {
      StringBuilder sb = new StringBuilder();
      f.getTests().forEach(t -> {
        sb.append(String.format("@Test(\"%s\") {\n", t.getTestName().replaceAll("\"", "\\\\\"")));
        sb.append(t.getCode().replaceAll("(?m)^", "  "));
        sb.append("\n}\n\n");
      });
      fileNameToText.put(f.getName() + ".zwl", sb.toString());
    });
    
    return fileNameToText;
  }
  
  Path composeAndPackageProjectFiles(List<ProjectDownloadableFile> files) throws IOException {
    Path zipFile = Paths.get(SYS_DEF_TEMP_DIR, UUID.randomUUID() + ".zip");
    try (ZipOutputStream s = new ZipOutputStream(
        new BufferedOutputStream(Files.newOutputStream(zipFile, StandardOpenOption.CREATE_NEW)))) {
      convertToZwlFile(files).forEach((k, v) -> {
        try {
          s.putNextEntry(new ZipEntry(k));
          s.write(v.getBytes(StandardCharsets.UTF_8));
          s.closeEntry();
        } catch (IOException io) {
          // This is fine for now
          throw new RuntimeException(io);
        }
      });
    }
    return zipFile;
  }
}
