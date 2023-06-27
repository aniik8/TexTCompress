# TexTCompress
A text Compressor based on Huffman coding (a lossless data compression algorithm). The idea is to assign variable-length codes to input characters, lengths of the assigned codes are based on the frequencies of corresponding characters. This algorithm builds a tree in bottom up manner

Steps 

1. Frequency Calculation

Read the input text file character by character and calculate the frequency of each character in the file.
Store the character frequencies in a data structure, such as a HashMap or an array.

2. Building the Huffman Tree

Create a HuffmanNode class to represent each node in the Huffman tree. Each node will have a character, a frequency, and left and right child nodes.
Build a priority queue (MinHeap) of HuffmanNodes based on their frequencies.
Remove the two nodes with the lowest frequencies from the priority queue, create a new node with their combined frequency, and make the two nodes its left and right children. Insert the new node back into the priority queue.
Repeat the above step until only one node remains in the priority queue. This node will be the root of the Huffman tree.

3. Generating Huffman Codes

Traverse the Huffman tree and assign binary codes to each character. 
For each character, the path from the root to the character in the tree will represent its Huffman code. Assign '0' for the left branch and '1' for the right branch.
Store the Huffman codes in a HashMap or any suitable data structure.

4. Compressing the File

Read the input text file again character by character.
Retrieve the Huffman code for each character from the HashMap.
Concatenate the Huffman codes for all characters and convert the resulting binary string to bytes.
Write the compressed binary data to an output file.

5. Creating the Decompressor

Read the compressed binary file.
Traverse the Huffman tree by following the binary bits one by one.
When reaching a leaf node, write the corresponding character to the output file.
Repeat the above step until all the bits in the compressed file have been processed.
