package vmTranslator;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Parser extends C_TYPES{
    static String ignorePattern ="^\\s*//.*|^\\s*";
    static Pattern cmdPattern = Pattern.compile("[a-z]+|[0-9]+");
    static Map<String, Short> commandMap = Map.ofEntries(
            Map.entry("add", C_ARITHMETIC),
            Map.entry("sub", C_ARITHMETIC),
            Map.entry("neg", C_ARITHMETIC),
            Map.entry("eq", C_ARITHMETIC),
            Map.entry("gt", C_ARITHMETIC),
            Map.entry("lt", C_ARITHMETIC),
            Map.entry("and", C_ARITHMETIC),
            Map.entry("or", C_ARITHMETIC),
            Map.entry("not", C_ARITHMETIC),
            Map.entry("push", C_PUSH),
            Map.entry("pop", C_POP)
    );

    private final Scanner vmFile;
    private String currentCommand;
    public int currentLineNum;
    private Matcher matcher;
    public Parser(String filePath) throws IOException {
        this.vmFile = new Scanner(Paths.get(filePath));
        this.currentLineNum = 0;
    }
    public boolean hasNextCommand(){
        return this.vmFile.hasNextLine();
    }
    public void advance(){
        this.currentCommand = this.vmFile.nextLine();
        this.currentLineNum += 1;
    }

    public short commandType(){
        if ( !this.currentCommand.matches(ignorePattern) ) {
            this.matcher = cmdPattern.matcher(this.currentCommand);
            if ( matcher.find() ){
                return commandMap.getOrDefault(matcher.group(), C_INVALID);
            }else{
                return C_INVALID;
            }
        }
        return C_EMPTY;
    }

    public String arg1(){
        String arg= this.matcher.group();
        if ( this.matcher.find() ){
            arg = this.matcher.group();
        }
        return arg;
    }
    public int arg2(){
        this.matcher.find();
        String arg = matcher.group();
        return Integer.parseInt(arg);
    }

    public void close(){
        this.vmFile.close();
    }
}
