package vmTranslator;

import java.io.IOException;

public class vmTranslator extends C_TYPES{
    public static void main(String[] args) throws IOException {
        String filename = "/Users/shuvo/Dropbox/repos/nand2tetris/projects/07/StackArithmetic/SimpleAdd/SimpleAdd.vm";
        Parser parser = new Parser(filename);
        //CodeWriter writer = new CodeWriter(filename.split("\\.")[0]+".asm");
        while (parser.hasNextCommand()){
            parser.advance();
            short c_type = parser.commandType();
            if ( c_type == C_INVALID ){
                System.out.println("Invalid command at line "+parser.currentLineNum);
                System.exit(0);
            }
            else if (c_type != C_EMPTY){
                System.out.println(c_type);
                System.out.println(parser.arg1());
                if (c_type != C_ARITHMATIC){
                    System.out.println(parser.arg2());
                }
            }
        }
    }
}
