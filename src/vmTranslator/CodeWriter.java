package vmTranslator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class CodeWriter {
    private final PrintWriter outputFile;
    static Map<String, String> arithSympol = Map.ofEntries(
            Map.entry("add", "+"),
            Map.entry("sub", "-"),
            Map.entry("neg", "-"),
            Map.entry("eq", "-"),
            Map.entry("lt", "-"),
            Map.entry("gt", "+"),
            Map.entry("and", "&"),
            Map.entry("or", "|"),
            Map.entry("not", "!")
    );
    public CodeWriter(String outputFile) throws IOException {
        this.outputFile = new PrintWriter( new FileWriter(outputFile) );
    }
    public void writeArithmatic(String command){
        this.outputFile.printf(arithSympol.get(command));
    }
    public void close(){
        this.outputFile.close();
    }
}
