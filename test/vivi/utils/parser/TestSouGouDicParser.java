package vivi.utils.parser;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import vivi.utils.domain.SouGouDic;
import vivi.utils.file.FileUtil;

public class TestSouGouDicParser {

    private static final String DIC_PATH = "data/dic/";
    private SouGouDicParser dicParser;
    
    @Before
    public void setUp() throws Exception {
        dicParser = new SouGouDicParser();
    }

    @Test
    public void testRead() throws IOException {
        SouGouDic dic = dicParser.read(new File(DIC_PATH + "政府机关团体机构大全.scel"));
        BufferedWriter writer = new BufferedWriter(new FileWriter(DIC_PATH + dic.getName() + ".dic"));
        for (List<String> words : dic.getPinyinToWords().values()) {
            writer = FileUtil.appendContents(writer, words);
        }
        writer.flush();
        writer.close();
        assertTrue(true);
    }

}
