package JackAnalyzer;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CompilationEngine {
    private final PrintWriter outputFile;
    private final JackTokenizer tokenizer;
    public CompilationEngine(JackTokenizer tokenizer, String outputFileName) throws IOException {
        this.outputFile = new PrintWriter( new FileWriter(outputFileName, false) );
        System.out.println(outputFileName);
        this.tokenizer = tokenizer;
    }
    public void compileClass() throws IOException {
        int i = 0;
        while ( this.tokenizer.hasMoreTokens() ){
            this.tokenizer.advance();
            System.out.println((i)+"th Token: "+this.tokenizer.currentToken);
            i += 1;
        }
    }
    public void close(){
        this.outputFile.close();
    }

}
