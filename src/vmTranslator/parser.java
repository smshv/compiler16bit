package vmTranslator;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Parser extends C_TYPES{
    static Pattern ignorePattern = Pattern.compile("//.*"); //^\s*//.*|^\s*)
    static Pattern cmdPattern = Pattern.compile("\\S+"); //[a-z-]+|[0-9]+
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
            Map.entry("pop", C_POP),
            Map.entry("label", C_LABEL),
            Map.entry("goto", C_GOTO),
            Map.entry("if-goto", C_IF),
            Map.entry("function", C_FUNCTION),
            Map.entry("call", C_CALL),
            Map.entry("return", C_RETURN)
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
        matcher = ignorePattern.matcher(this.currentCommand);
        this.currentCommand =  matcher.find() ? this.currentCommand.substring(0, matcher.start()) : this.currentCommand; //weedout the comments
        this.currentLineNum += 1;
    }

    public short commandType(){
        if ( this.currentCommand.length() > 1 ) {
            matcher = cmdPattern.matcher(this.currentCommand);
            if ( matcher.find() ){
                return commandMap.getOrDefault(matcher.group(), C_INVALID);
            }else{
                return C_EMPTY;
            }
        }
        return C_EMPTY;
    }

    public String arg1(){
        String arg= matcher.group();
        if ( matcher.find() ){
            arg = matcher.group();
        }
        return arg;
    }
    public int arg2(){
        matcher.find();
        String arg = matcher.group();
        return Integer.parseInt(arg);
    }

    public void close(){
        this.vmFile.close();
    }
}
