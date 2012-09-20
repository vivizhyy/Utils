package vivi.utils.domain;

import java.util.List;
import java.util.Map;

public class SouGouDic {

    private Map<String, List<String>> pinyinToWords;
    private String name;
    private String type;
    private String description;
    private String sampleWord;
    
    public Map<String, List<String>> getPinyinToWords() {
        return pinyinToWords;
    }
    public void setPinyinToWords(Map<String, List<String>> pinyinToWord) {
        this.pinyinToWords = pinyinToWord;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getSampleWord() {
        return sampleWord;
    }
    public void setSampleWord(String sampleWord) {
        this.sampleWord = sampleWord;
    }
    
    @Override
    public String toString() {
        return "SouGouDic [pinyinToWords=" + pinyinToWords + ", name=" + name
                + ", type=" + type + ", description=" + description
                + ", sampleWord=" + sampleWord + "]";
    }
    
}
