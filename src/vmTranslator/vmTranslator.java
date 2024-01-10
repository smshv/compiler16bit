package vmTranslator;

import java.io.IOException;
import java.io.File;
import java.sql.Array;

public class vmTranslator extends C_TYPES{
    private static void procVMfile(String filename, CodeWriter writer) throws IOException {
        Parser parser = new Parser(filename);
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
                }else if ( c_type == C_LABEL ){
                    //System.out.println(parser.arg1());
                    writer.writeLabel(parser.arg1());
                }else if ( c_type == C_GOTO ){
                    writer.writeGoTo(parser.arg1());
                }else if ( c_type == C_IF ){
                    writer.writeIf(parser.arg1());
                }else if ( c_type == C_FUNCTION ){
                    writer.writeFunction(parser.arg1(), parser.arg2());
                }else if ( c_type == C_CALL ){
                    writer.writeCall(parser.arg1(), parser.arg2());
                }else { //must be C_LABEL
                    writer.writeReturn();
                }
            }
        }
        parser.close();
    }
    public static void main(String[] args) throws IOException {
        File inputFile = new File(args[0]);
        File[] dirList;
        String outputFileName;

        if ( inputFile.isFile() ){
            dirList = new File[]{inputFile};
            outputFileName = args[0].split("\\.")[0]+".asm";
        }else{
            dirList = inputFile.listFiles();
            String[] tmp = args[0].split("/");
            outputFileName = args[0].charAt(args[0].length()-1)=='/' ? args[0] + tmp[tmp.length-1]+".asm" : args[0]+"/"+tmp[tmp.length-1]+".asm";
        }
        System.out.println(outputFileName);
        if (dirList != null){
            CodeWriter writer = new CodeWriter(outputFileName);
            writer.writeInit();
            for (File file: dirList){
                if ( file.isFile() && file.getName().endsWith(".vm")){
                    writer.setFileName(file.getName());
                    procVMfile(file.getPath(), writer);
                }
            }
            writer.close();
        }
    }
}
