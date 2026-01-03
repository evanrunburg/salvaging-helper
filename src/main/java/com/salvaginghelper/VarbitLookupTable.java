package com.salvaginghelper;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;

// Values taken from net.runelite.api.gameval.* on 12/16/2025

public class VarbitLookupTable {

    private Map<String, String> LOOKUP_TABLE;
    Properties varbitProperties;

    public VarbitLookupTable(String filePath) {
        varbitProperties = new Properties();
        try (InputStream input = new FileInputStream(filePath)){
            varbitProperties.load(input);
        } catch (IOException e) {
            return;
        }
        LOOKUP_TABLE = new HashMap<String, String>((Map)varbitProperties);
    }

    public String toGameVal(int varbitId) {
        return LOOKUP_TABLE.getOrDefault(Integer.toString(varbitId), "-2");
    }
}