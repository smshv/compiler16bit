package vmTranslator;

import java.io.IOException;

public class VMTranslator extends C_TYPES{
    public static void main(String[] args) throws IOException {
        String filename = args[0];
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
                    writer.writeArithmetic(parser.arg1());
                }else if(c_type == C_PUSH || c_type == C_POP){
                    writer.writePushPop(c_type, parser.arg1(), parser.arg2());
                }
            }
        }
        parser.close();
        writer.close();
    }
}
