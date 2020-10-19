package br.unisinos.encodedecodestepbystep.service.codification;


import br.unisinos.encodedecodestepbystep.repository.ReaderInterface;
import br.unisinos.encodedecodestepbystep.repository.WriterInterface;
import br.unisinos.encodedecodestepbystep.repository.redundancy.WriterRedundancy;
import br.unisinos.encodedecodestepbystep.service.redundancy.CRCService;
import br.unisinos.encodedecodestepbystep.utils.StringUtils;
import br.unisinos.encodedecodestepbystep.utils.exceptions.WrongFormatExpection;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UnarioService implements CodificationService {
    @Override
    public void encode(WriterInterface writer, ReaderInterface reader) throws IOException, WrongFormatExpection {
        writer.writeSemHamming(getBitsIdentificacaoAlgoritmo(writer));

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

            writer.write(codeword);
            character = reader.read();
        }
        reader.close();
        writer.close();
    }

    @Override
    public void decode(WriterInterface writer, ReaderInterface reader) throws IOException, WrongFormatExpection {
        reader.readCabecalho();// apenas para passar os bits do cabeçalho

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

    @Override
    public String getBitsIdentificacaoAlgoritmo(WriterInterface writer) {
        String firstByte = "00011111"; //identificaçãoAlgoritmo
        String secondByte = "00000000"; // informação extra goloumb
        if (writer instanceof WriterRedundancy) {
            CRCService crcService = new CRCService();
            String encodedCRC = crcService.calculateCRC8(firstByte, secondByte);
            return firstByte + secondByte + encodedCRC;
        }
        return firstByte + secondByte;
    }
}
