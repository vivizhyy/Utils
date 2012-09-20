package vivi.utils.parser;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import vivi.utils.domain.SouGouDic;

public class SouGouDicParser {

    private static final String ENCODING = "UTF-16LE";
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    
    public SouGouDic read(File file) throws IOException {
        return read(new FileInputStream(file));
    }

    private String readString(DataInputStream dataInputStream, int pos, int[] reads) throws IOException {
        int read = reads[0];
        dataInputStream.skip(pos-read);
        read = pos;
        byteArrayOutputStream.reset();
        while(true) {
            int c1 = dataInputStream.read();
            int c2 = dataInputStream.read();
            read += 2;
            if(c1==0 && c2==0) {
                break;
            } else {
                byteArrayOutputStream.write(c1);
                byteArrayOutputStream.write(c2);
            }
        }
        reads[0] = read;
        return new String(byteArrayOutputStream.toByteArray(), ENCODING);
    }

    private SouGouDic read(InputStream in) throws IOException {
        SouGouDic souGouDic = new SouGouDic();
        DataInputStream dataInputStream = new DataInputStream(in);
        int read;
        try {
            byte[] bytes = new byte[4];
            dataInputStream.readFully(bytes);
            assert (bytes[0] == 0x40 && bytes[1] == 0x15 && bytes[2] == 0 && bytes[3] == 0);
            dataInputStream.readFully(bytes);
            int flag1 = bytes[0];
            assert (bytes[1] == 0x43 && bytes[2] == 0x53 && bytes[3] == 0x01);
            int[] reads=new int[]{8};
            souGouDic.setName(readString(dataInputStream, 0x130, reads));
            souGouDic.setType(readString(dataInputStream, 0x338, reads));
            souGouDic.setDescription(readString(dataInputStream, 0x540, reads));
            souGouDic.setSampleWord(readString(dataInputStream, 0xd40, reads));
            read = reads[0];
            dataInputStream.skip(0x1540 - read);
            read = 0x1540;
            dataInputStream.readFully(bytes);
            read += 4;
            assert (bytes[0] == (byte) 0x9D && bytes[1] == 0x01 && bytes[2] == 0 && bytes[3] == 0);
            bytes = new byte[128];
            Map<Integer, String> idToPinyin = new LinkedHashMap<Integer, String>();
            while (true) {
                int mark = readUnsignedShort(dataInputStream);
                int size = dataInputStream.readUnsignedByte();
                dataInputStream.skip(1);
                read += 4;
                assert (size > 0 && (size % 2) == 0);
                dataInputStream.readFully(bytes, 0, size);
                read += size;
                String pinyin = new String(bytes, 0, size, ENCODING);
                idToPinyin.put(mark, pinyin);
                if ("zuo".equals(pinyin)) {
                    break;
                }
            }
            if (flag1 == 0x44) {
                dataInputStream.skip(0x2628 - read);
            } else if (flag1 == 0x45) {
                dataInputStream.skip(0x26C4 - read);
            } else {
                throw new RuntimeException("SouGou data structure changed");
            }
            StringBuffer buffer = new StringBuffer();
            Map<String, List<String>> pinyinToWords = new LinkedHashMap<String, List<String>>();
            while (true) {
                int size = readUnsignedShort(dataInputStream);
                if (size < 0) {
                    break;
                }
                int count = readUnsignedShort(dataInputStream);
                int len = count / 2;
                assert (len * 2 == count);
                buffer.setLength(0);
                for (int i = 0; i < len; i++) {
                    int key = readUnsignedShort(dataInputStream);
                    buffer.append(idToPinyin.get(key));
                }
                buffer.setLength(buffer.length() - 1);
                String pinyin = buffer.toString();
                List<String> list = pinyinToWords.get(pinyin);
                if (list == null) {
                    list = new ArrayList<>();
                    pinyinToWords.put(pinyin, list);
                }
                for (int i = 0; i < size; i++) {
                    count = readUnsignedShort(dataInputStream);
                    if (count > bytes.length) {
                        bytes = new byte[count];
                    }
                    dataInputStream.readFully(bytes, 0, count);
                    String word = new String(bytes, 0, count, ENCODING);
                    //接下来12个字节可能是词频或者类似信息
                    dataInputStream.skip(12);
                    list.add(word);
                }
            }
            souGouDic.setPinyinToWords(pinyinToWords);
            return souGouDic;
        } finally {
            in.close();
        }
    }

    private final int readUnsignedShort(InputStream in) throws IOException {
        int ch1 = in.read();
        int ch2 = in.read();
        if ((ch1 | ch2) < 0) {
            return Integer.MIN_VALUE;
        }
        return (ch2 << 8) + (ch1 << 0);
    }
}
