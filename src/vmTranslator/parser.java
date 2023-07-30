package vmTranslator;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Parser extends C_TYPES{
    static String ignorePattern ="^\s*//.*|^\s*";
    static Pattern cmdPattern = Pattern.compile("[a-z]+|[0-9]+");
    static Map<String, Short> commandMap = Map.ofEntries(
            Map.entry("add", C_ARITHMATIC),
            Map.entry("sub", C_ARITHMATIC),
            Map.entry("neg", C_ARITHMATIC),
            Map.entry("eq", C_ARITHMATIC),
            Map.entry("gt", C_ARITHMATIC),
            Map.entry("lt", C_ARITHMATIC),
            Map.entry("and", C_ARITHMATIC),
            Map.entry("or", C_ARITHMATIC),
            Map.entry("not", C_ARITHMATIC),
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
            matcher.find();
            String matchedString = matcher.group();
            String possibleCommand = matchedString;
            if (commandMap.containsKey(possibleCommand)) {
                return commandMap.get(possibleCommand);
            } else {
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
        return Integer.valueOf(arg);
    }

    public void close(){
        this.vmFile.close();
    }
}
