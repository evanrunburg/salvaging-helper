package com.salvaginghelper;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;

// Values taken from net.runelite.api.gameval.* on 12/16/2025

public class LookupTable {

    private Map<String, String> LOOKUP_TABLE;
    Properties objectProperties;

    public LookupTable(String filePath) {
        objectProperties = new Properties();
        try (InputStream input = new FileInputStream(filePath)){
            objectProperties.load(input);
        } catch (IOException e) {
            return;
        }
        LOOKUP_TABLE = new HashMap<String, String>((Map) objectProperties);
    }

    public String toVal(int varbitId) {
        return LOOKUP_TABLE.getOrDefault(Integer.toString(varbitId), "-2");
    }
}