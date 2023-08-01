package vmTranslator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class CodeWriter extends C_TYPES{
    private final String fileName;
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
    static Map<String, String> segmentPointerMap = Map.ofEntries(
            Map.entry("local", "LCL"),
            Map.entry("argument", "ARG"),
            Map.entry("this", "THIS"),
            Map.entry("that", "THAT")
    );
    static String putToStack = "@SP\n" +
            "A=M\n" + "M=D\n";

    static String getFromStack = "@SP\n" +
            "A=M\n" + "D=M\n";
    static String decStackPointer = "@SP\n"+
            "M=M-1\n";
    static String incStackPointer = "@SP\n"+
            "M=M+1\n";
    static String writeToR13 = "@R13\n" +   //R13 is used for stroing pointer
            "M=D\n";
    static String pointToAddrInR13 = "@R13\n"+
            "A=M\n";

    public CodeWriter(String outputFile) throws IOException {
        this.fileName = outputFile;
        this.outputFile = new PrintWriter( new FileWriter(outputFile+".asm", false) );
    }
    public void writeArithmetic(String command){
        this.outputFile.write("// "+command+"\n");
        this.outputFile.write(decStackPointer);
        if ( command.equals("neg") || command.equals("not") ){
            this.outputFile.write("@SP\nA=M\n");
            this.outputFile.write("M="+arithSympol.get(command)+"M\n"); //No need get to D reg and then
            //put it back to stack
        }else{
            this.outputFile.write(getFromStack);
            this.outputFile.write(decStackPointer);
            this.outputFile.write("@SP\nA=M\n");
            this.outputFile.write("D=D"+arithSympol.get(command)+"M\n");
            this.outputFile.write(putToStack);
        }
        this.outputFile.write(incStackPointer);
    }
    private void translatedPushPop(short c_type, String setupAddr, String segment, Integer index){
        if (c_type == C_PUSH){
            this.outputFile.write("// push "+segment+" "+index+"\n");
            this.outputFile.write(setupAddr);
            this.outputFile.write("A=A+D\n");
            this.outputFile.write("D=M\n");
            this.outputFile.write(putToStack);
            this.outputFile.write(incStackPointer);
        }else{
            this.outputFile.write("// pop "+segment+" "+index+"\n");
            this.outputFile.write(setupAddr);
            this.outputFile.write("D=D+A\n");
            this.outputFile.write(writeToR13);
            this.outputFile.write(decStackPointer);
            this.outputFile.write(getFromStack);
            this.outputFile.write(pointToAddrInR13);
            this.outputFile.write("M=D\n");
        }
    }
    public void writePushPop(short c_type, String segment, Integer index){
        String setupAddr;
        if ( segment.equals("constant") ){
            if ( c_type == C_PUSH ) {
                this.outputFile.write("// push "+segment+" "+index+"\n");
                setupAddr = "@" + index.toString() + "\n";
                this.outputFile.write(setupAddr);
                this.outputFile.write("D=A\n");
                this.outputFile.write(putToStack);
                this.outputFile.write(incStackPointer);
            }
            return;
        }else if( segment.equals("static") ){
            setupAddr = "@"+this.fileName+"."+index.toString()+"\n";
            if ( c_type == C_POP ) {
                this.outputFile.write("// pop "+segment+" "+index+"\n");
                popWithNoPointer(setupAddr);
                return;
            }

        }else if ( segment.equals("local") ) {
            setupAddr = "@LCL\n"+"D=M\n"+("@"+index.toString()+"\n")+"AD=A+D\n";
        } else if ( segment.equals("argument") ) {
            setupAddr = "@ARG\n"+"D=A\n"+("@"+index.toString()+"\n")+"AD=A+D\n";
        } else if ( segment.equals("this") ) {
            setupAddr = "@THIS\n"+"D=A\n"+("@"+index.toString()+"\n")+"AD=A+D\n";
        } else if ( segment.equals("that") ) {
            setupAddr = "@THAT\n"+"D=A\n"+("@"+index.toString()+"\n")+"AD=A+D\n";
        } else if ( segment.equals("temp") ) {
            setupAddr = "@5\n"+"D=A\n"+("@"+index.toString()+"\n")+"AD=A+D\n";
        } else if ( segment.equals("pointer") ){
            setupAddr = (index == 0 ? "@this\n" : "@that\n");
            if ( c_type == C_POP ) {
                this.outputFile.write("// pop "+segment+" "+index+"\n");
                popWithNoPointer(setupAddr);
                return;
            }
        }
        else{
            setupAddr="";
            System.out.println("Invalid segment "+segment);
            System.exit(0);
        }
        if ( c_type == C_PUSH ){
            this.outputFile.write("// push "+segment+" "+index+"\n");
            this.outputFile.write(setupAddr);
            this.outputFile.write("D=M\n");
            this.outputFile.write(putToStack);
            this.outputFile.write(incStackPointer);
        }else{
            this.outputFile.write("// pop "+segment+" "+index+"\n");
            this.outputFile.write(setupAddr);
            this.outputFile.write(writeToR13);
            this.outputFile.write(decStackPointer);
            this.outputFile.write(getFromStack);
            this.outputFile.write(pointToAddrInR13);
            this.outputFile.write("M=D\n");
        }
    }

    private void popWithNoPointer(String setupAddr) {
        this.outputFile.write(decStackPointer);
        this.outputFile.write(getFromStack);
        this.outputFile.write(setupAddr);
        this.outputFile.write("M=D\n");
    }

    public void close(){
        this.outputFile.close();
    }
}
