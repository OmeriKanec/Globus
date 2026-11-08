package module14;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class FileCopyBenchmark {
    private static final int BUFFER_SIZE = 64 * 1024;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: FileCopyBenchmark <source-file> <new-output-directory>");
            System.exit(1);
        }

        Path source = Path.of(args[0]);
        if (!Files.isRegularFile(source)) {
            throw new IOException("Source must be a regular file: " + source);
        }

        long sourceSize = Files.size(source);
        Path output = Files.createDirectory(Path.of(args[1]));
        Path unbuffered = output.resolve("byte-by-byte.bin");
        Path buffered = output.resolve("buffered.bin");
        System.out.printf(Locale.ROOT, "Source: %d bytes (%.2f MiB)%n",
                sourceSize, sourceSize / (1024.0 * 1024));

        long start = System.nanoTime();
        copyByteByByte(source, unbuffered);
        long unbufferedNanos = System.nanoTime() - start;
        System.out.printf(Locale.ROOT, "Byte by byte: %.3f ms%n", unbufferedNanos / 1_000_000.0);

        start = System.nanoTime();
        copyBuffered(source, buffered);
        long bufferedNanos = System.nanoTime() - start;
        System.out.printf(Locale.ROOT, "Buffered (64 KiB): %.3f ms%n", bufferedNanos / 1_000_000.0);

        if (Files.mismatch(source, unbuffered) != -1 || Files.mismatch(source, buffered) != -1) {
            throw new IOException("Copy content differs from the source");
        }
        System.out.printf(Locale.ROOT, "Speedup: %.2fx%n", (double) unbufferedNanos / bufferedNanos);
        System.out.println("Both copies match the source byte for byte.");
    }

    private static void copyByteByByte(Path source, Path target) throws IOException {
        try (FileInputStream input = new FileInputStream(source.toFile());
             FileOutputStream output = new FileOutputStream(target.toFile())) {
            int nextByte;
            while ((nextByte = input.read()) != -1) {
                output.write(nextByte);
            }
        }
    }

    private static void copyBuffered(Path source, Path target) throws IOException {
        try (BufferedInputStream input = new BufferedInputStream(
                     new FileInputStream(source.toFile()), BUFFER_SIZE);
             BufferedOutputStream output = new BufferedOutputStream(
                     new FileOutputStream(target.toFile()), BUFFER_SIZE)) {
            byte[] chunk = new byte[BUFFER_SIZE / 2];
            int bytesRead;
            while ((bytesRead = input.read(chunk)) != -1) {
                output.write(chunk, 0, bytesRead);
            }
        }
    }
}
//Source: 105906176 bytes (101.00 MiB)
//Byte by byte: 200155.984 ms
//Buffered (64 KiB): 55.046 ms
//Такое ускорение из-за очень большого сокращения количества обращений к потокам, а значит и расходов на это