package com.salvaginghelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestLookupTable
{
    public static void main(String[] args) throws IOException {
        //VarbitLookupTable v = new VarbitLookupTable();
        //System.out.println(VarbitLookupTable.toGameVal(26));
        Path filePath = Paths.get("test.txt");
        System.out.println(filePath.toString());
        Files.writeString(filePath, "aaaaa");
    }
}
