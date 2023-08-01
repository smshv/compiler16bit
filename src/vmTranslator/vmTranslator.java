package vmTranslator;

import java.io.IOException;

public class vmTranslator extends C_TYPES{
    public static void main(String[] args) throws IOException {
        //String filename = "/Users/shuvo/Dropbox/repos/nand2tetris/projects/07/StackArithmetic/SimpleAdd/SimpleAdd.vm";
        String filename = "/Users/shuvo/Dropbox/repos/nand2tetris/projects/07/StackArithmetic/StackTest/StackTest.vm";
        Parser parser = new Parser(filename);
        CodeWriter writer = new CodeWriter(filename.split("\\.")[0]);
        while (parser.hasNextCommand()){
            parser.advance();
            short c_type = parser.commandType();
            if ( c_type == C_INVALID ){
                System.out.println("Invalid command at line "+parser.currentLineNum);
                System.exit(0);
            }
            else if (c_type != C_EMPTY){
                if ( c_type == C_ARITHMETIC){
                    //System.out.println(C_ARITHMETIC);
                    //System.out.println(parser.arg1());
                    writer.writeArithmetic(parser.arg1());
                }else if(c_type == C_PUSH || c_type == C_POP){
                    //System.out.println(c_type);
                    //System.out.println(parser.arg1());
                    //System.out.println(parser.arg2());
                    writer.writePushPop(c_type, parser.arg1(), parser.arg2());
                }
            }
        }
        parser.close();
        writer.close();
    }
}
