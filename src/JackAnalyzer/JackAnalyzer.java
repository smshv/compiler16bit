package JackAnalyzer;

import java.io.FileNotFoundException;
import java.io.File;
import java.io.IOException;

public class JackAnalyzer {
    public static void main(String[] args) throws IOException {
        File inputFile = new File(args[0]);
        File[] dirList = inputFile.isFile() ? new File[]{inputFile} : inputFile.listFiles();

        if ( dirList != null ){
            for (File file: dirList) {
                if ( file.getName().endsWith(".jack") ){
                    JackTokenizer tokenizer = new JackTokenizer(file);
                    CompilationEngine compiler = new CompilationEngine(tokenizer, file.getPath().substring(0, file.getPath().length()-4)+"xml");
                    compiler.compileClass();
                    compiler.close();
                    tokenizer.close();
                }
            }
        }

    }
}
