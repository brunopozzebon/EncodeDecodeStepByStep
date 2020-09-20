package codifications;

import utils.Reader;
import utils.StringUtils;
import utils.Writer;

import java.io.File;
import java.io.IOException;

import static codifications.Constants.*;

public class Unario implements Codification {
    @Override
    public void encode(File file) throws IOException {
        Reader reader = new Reader(file);
        Writer writer = new Writer(ENCODED_FOLDER + file.getName() + EXTENSION);
        String bits = "";

        int character = reader.read();
        int bit = 0;
        while (character != -1) {
            String codeword = "";
            if (bit == 0) {
                codeword = StringUtils.createStreamOnZeros(character);
                bit = 1;
            } else {
                codeword = StringUtils.createStreamWithOnes(character);
                bit = 0;
            }
            bits = bits.concat(codeword);
            bits = writer.gravaBitsEmPartesDe8ERetornaOResto(bits);
            character = reader.read();
        }
        if (bits.length() != 0) {
            writer.write(bits);
        }
        reader.close();
        writer.close();
    }

    @Override
    public void decode(File file) throws IOException {
        Reader reader = new Reader(file);
        Writer writer = new Writer(DECODED_FOLDER + file.getName());

        int bitRead = reader.readNextChar();
        int last = bitRead;
        int counter = 0;

        while (bitRead != -1) {
            bitRead = reader.readNextChar();
            counter++;
            if (bitRead != last) {
                char character = (char) counter;
                writer.write(character);
                last = bitRead;
                counter = 0;
            }
        }

        reader.close();
        writer.close();
    }
}