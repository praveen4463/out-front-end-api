package com.zylitics.front.api;

import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;

import java.nio.ByteBuffer;

class FileDownload {
  
  byte[] download(Blob blob) {
    if (blob.getSize() > Integer.MAX_VALUE) {
      throw new RuntimeException("Won't download blob of this size " + blob.getSize());
    }
    int size = blob.getSize().intValue();
    byte[] writeTo = new byte[size];
    ByteBuffer writeBuffer = ByteBuffer.wrap(writeTo);
    try {
      if (blob.getSize() < 1_000_000) {
        return blob.getContent();
      } else {
        try (ReadChannel reader = blob.reader()) {
          ByteBuffer bytes = ByteBuffer.allocate(64 * 1024); // 64 kilo bytes buffer
          while (reader.read(bytes) > 0) {
            bytes.flip();
            writeBuffer.put(bytes);
            bytes.clear();
          }
        }
      }
    } catch (Exception io) {
      // We should be catching only storage related exceptions here, IO error should be handled
      // in a separate try-catch if desired.
      // for now no reattempt or catching StorageException separately, just log and see what
      // errors we get.
      // TODO: watch exceptions and decide on reattempts and what to notify user
      throw new RuntimeException(io); // don't force caller handle an exception.
    }
    return writeTo;
  }
}
