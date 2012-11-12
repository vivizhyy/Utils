package vivi.xiami;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class TestAutoFiller {

//    XiamiAudio autoFiller;
    private static final String path = "C:\\Users\\yyzhang\\xiami\\";
    
    @Before
    public void setUp() throws Exception {
//        autoFiller = new XiamiAudio("C:\\Users\\yyzhang\\xiami\\Blue_Jeans_Album_Version_Remastered_虾小米打碟中_03_1770760764_2984092.mp3");
    }

    @Test
    public void isXiaminFile() throws Exception {
        for (String filename : new File(path).list()) {
            XiamiAudio xiamiAudio = XiamiAudio.build(path + filename);
            if (xiamiAudio != null) {
                xiamiAudio.extactFieldsInfo();
                xiamiAudio.updateAudioInfo();
                System.out.println(xiamiAudio.printTagFields());
            }
        }
//        assertTrue(autoFiller.isXiaminFile("Blue_Jeans_Album_Version_Remastered_虾小米打碟中_03_1770760764_2984092.mp3"));
        
    }

}
