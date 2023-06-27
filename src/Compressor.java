import java.io.*;
import java.util.*;

class HuffmanNode implements Comparable<HuffmanNode> {
    char character;
    int frequency;
    HuffmanNode left, right;

    public HuffmanNode(char character, int frequency, HuffmanNode left, HuffmanNode right) {
        this.character = character;
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    @Override
    public int compareTo(HuffmanNode other) {
        return this.frequency - other.frequency;
    }
}

class HuffmanCompressor {
    private Map<Character, String> huffmanCodes = new HashMap<>();

    public void compressFile(String inputFilePath, String outputFilePath) {
        // Step 1: Frequency Calculation
        Map<Character, Integer> frequencies = calculateFrequencies(inputFilePath);

        // Step 2: Building the Huffman Tree
        HuffmanNode root = buildHuffmanTree(frequencies);

        // Step 3: Generating Huffman Codes
        generateHuffmanCodes(root, "");

        try {
            // Step 4: Compressing the File
            FileInputStream inputFileStream = new FileInputStream(inputFilePath);
            BitOutputStream bitOutputStream = new BitOutputStream(outputFilePath);

            // Write the frequency table to the compressed file
            writeFrequencyTable(frequencies, bitOutputStream);

            // Write the compressed data to the file
            int character;
            while ((character = inputFileStream.read()) != -1) {
                char c = (char) character;
                String code = huffmanCodes.get(c);
                for (char bit : code.toCharArray()) {
                    bitOutputStream.writeBit(bit - '0');
                }
            }

            inputFileStream.close();
            bitOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decompressFile(String inputFilePath, String outputFilePath) {
        try {
            // Step 5: Creating the Decompressor
            BitInputStream bitInputStream = new BitInputStream(inputFilePath);
            FileOutputStream outputFileStream = new FileOutputStream(outputFilePath);

            // Read the frequency table from the compressed file
            Map<Character, Integer> frequencies = readFrequencyTable(bitInputStream);

            // Build the Huffman tree using the frequency table
            HuffmanNode root = buildHuffmanTree(frequencies);

            int bit;
            HuffmanNode currentNode = root;

            // Traverse the Huffman tree and write the decompressed characters to the output file
            while ((bit = bitInputStream.readBit()) != -1) {
                if (bit == 0) {
                    currentNode = currentNode.left;
                } else {
                    currentNode = currentNode.right;
                }

                if (currentNode.left == null && currentNode.right == null) {
                    outputFileStream.write(currentNode.character);
                    currentNode = root;
                }
            }

            bitInputStream.close();
            outputFileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<Character, Integer> calculateFrequencies(String filePath) {
        Map<Character, Integer> frequencies = new HashMap<>();

        try (FileReader fileReader = new FileReader(filePath)) {
            int character;
            while ((character = fileReader.read()) != -1) {
                char c = (char) character;
                frequencies.put(c, frequencies.getOrDefault(c, 0) + 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return frequencies;
    }

    private HuffmanNode buildHuffmanTree(Map<Character, Integer> frequencies) {
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();

        for (Map.Entry<Character, Integer> entry : frequencies.entrySet()) {
            char character = entry.getKey();
            int frequency = entry.getValue();
            priorityQueue.offer(new HuffmanNode(character, frequency, null, null));
        }

        while (priorityQueue.size() > 1) {
            HuffmanNode leftNode = priorityQueue.poll();
            HuffmanNode rightNode = priorityQueue.poll();
            HuffmanNode newNode = new HuffmanNode('\0', leftNode.frequency + rightNode.frequency, leftNode, rightNode);
            priorityQueue.offer(newNode);
        }

        return priorityQueue.poll();
    }

    private void generateHuffmanCodes(HuffmanNode node, String code) {
        if (node.left == null && node.right == null) {
            huffmanCodes.put(node.character, code);
            return;
        }

        generateHuffmanCodes(node.left, code + "0");
        generateHuffmanCodes(node.right, code + "1");
    }

    private void writeFrequencyTable(Map<Character, Integer> frequencies, BitOutputStream bitOutputStream) throws IOException {
        bitOutputStream.writeInt(frequencies.size());

        for (Map.Entry<Character, Integer> entry : frequencies.entrySet()) {
            char character = entry.getKey();
            int frequency = entry.getValue();

            bitOutputStream.writeChar(character);
            bitOutputStream.writeInt(frequency);
        }
    }

    private Map<Character, Integer> readFrequencyTable(BitInputStream bitInputStream) throws IOException {
        int tableSize = bitInputStream.readInt();
        Map<Character, Integer> frequencies = new HashMap<>();

        for (int i = 0; i < tableSize; i++) {
            char character = bitInputStream.readChar();
            int frequency = bitInputStream.readInt();
            frequencies.put(character, frequency);
        }

        return frequencies;
    }
}

class BitOutputStream implements Closeable {
    private OutputStream outputStream;
    private int buffer;
    private int bitsRemaining;

    public BitOutputStream(String filePath) throws IOException {
        outputStream = new FileOutputStream(filePath);
        buffer = 0;
        bitsRemaining = 8;
    }

    public void writeBit(int bit) throws IOException {
        buffer <<= 1;
        buffer |= bit;

        bitsRemaining--;

        if (bitsRemaining == 0) {
            outputStream.write(buffer);
            buffer = 0;
            bitsRemaining = 8;
        }
    }

    public void writeInt(int value) throws IOException {
        outputStream.write((value >>> 24) & 0xFF);
        outputStream.write((value >>> 16) & 0xFF);
        outputStream.write((value >>> 8) & 0xFF);
        outputStream.write(value & 0xFF);
    }

    public void writeChar(char c) throws IOException {
        outputStream.write((c >>> 8) & 0xFF);
        outputStream.write(c & 0xFF);
    }

    @Override
    public void close() throws IOException {
        if (bitsRemaining < 8) {
            buffer <<= bitsRemaining;
            outputStream.write(buffer);
        }

        outputStream.close();
    }
}

class BitInputStream implements Closeable {
    private InputStream inputStream;
    private int buffer;
    private int bitsRemaining;

    public BitInputStream(String filePath) throws IOException {
        inputStream = new FileInputStream(filePath);
        buffer = 0;
        bitsRemaining = 0;
    }

    public int readBit() throws IOException {
        if (bitsRemaining == 0) {
            buffer = inputStream.read();
            if (buffer == -1) {
                return -1;
            }
            bitsRemaining = 8;
        }

        int bit = (buffer >>> (bitsRemaining - 1)) & 1;
        bitsRemaining--;

        return bit;
    }

    public int readInt() throws IOException {
        int value = 0;
        value |= (inputStream.read() & 0xFF) << 24;
        value |= (inputStream.read() & 0xFF) << 16;
        value |= (inputStream.read() & 0xFF) << 8;
        value |= inputStream.read() & 0xFF;
        return value;
    }

    public char readChar() throws IOException {
        char c = (char) ((inputStream.read() << 8) & 0xFF00);
        c |= inputStream.read() & 0xFF;
        return c;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}

public class Compressor {
    public static void main(String[] args) {
        HuffmanCompressor compressor = new HuffmanCompressor();
        String inputFilePath = "/Users/jaibhole/Documents/DSA/src/input.txt";
        String compressedFilePath = "compressed.bin";
        String decompressedFilePath = "decompressed.txt";

        // Compress the file
        compressor.compressFile(inputFilePath, compressedFilePath);

        // Decompress the file
        compressor.decompressFile(compressedFilePath, decompressedFilePath);
    }
}
