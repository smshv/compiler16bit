package vmTranslator;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Parser extends C_TYPES{
    static Pattern commandPattern = Pattern.compile("^\\s*\\w+\\s+");
    static Pattern arg1Pattern = Pattern.compile("\\s+\\w+\\s+");
    static Pattern arg2Pattern = Pattern.compile("\\s+\\d+\\s+$");
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
    public Parser(String filePath){
        this.vmFile = new Scanner(filePath);
    }
    public boolean hasNextCommand(){
        return this.vmFile.hasNextLine();
    }
    public void advance(){
        this.currentCommand = this.vmFile.nextLine();
    }

    private  String getMatchedString(Pattern pattern){
        Matcher matcher = pattern.matcher(this.currentCommand);
        return this.currentCommand.substring(matcher.start(), matcher.end());
    }

    public short commandType(){
        String possibleCommand = getMatchedString(commandPattern);
        if ( commandMap.containsKey( possibleCommand ) ) {
            return commandMap.get(possibleCommand);
        }else{
            return C_INVALID;
        }
    }

    public String arg1(){
        return getMatchedString(arg1Pattern);
    }
    public int arg2(){
        return Integer.valueOf(getMatchedString(arg2Pattern));
    }

    public void close(){
        this.vmFile.close();
    }
}
