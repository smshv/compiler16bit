package vmTranslator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class CodeWriter extends C_TYPES{
    private final String fileName;
    private final PrintWriter outputFile;
    private int trueLabelNum;
    private int endLabelNum;
    static String putToStack = "@SP\n"+"A=M\n"+"M=D\n"+"@SP\n"+"M=M+1\n";
    static String pointToStackTop = "@SP\n"+"AM=M-1\n";
    static String writeToR13 = "@R13\n" +   //R13 is used for stroing pointer
            "M=D\n";
    static String pointToAddrInR13 = "@R13\n"+
            "A=M\n";

    public CodeWriter(String outputFile) throws IOException {
        String[] tmp = outputFile.split("/");
        this.fileName = tmp[tmp.length-1];
        this.outputFile = new PrintWriter( new FileWriter(outputFile+".asm", false) );
        this.trueLabelNum = 0;
        this.endLabelNum = 0;
    }
    public void writeConditionalArithmetic(String jumpCommand){
        String trueLabel = "true_"+this.trueLabelNum;
        String endLabel = "end_"+this.endLabelNum;
        this.trueLabelNum += 1;
        this.endLabelNum += 1;
        this.outputFile.write("D=M\n"+pointToStackTop);
        this.outputFile.write("D=M-D\n"+String.format("@%s\n", trueLabel)+
                String.format("D;%s\n",jumpCommand)+"D=0\n");
        this.outputFile.write(String.format("@%s\n",endLabel)+"0;JMP\n"+
                String.format("(%s)\n",trueLabel)+"D=-1\n"+String.format("(%s)\n", endLabel));
        this.outputFile.write("@SP\n"+"A=M\n"+"M=D\n");

    }
    public void writeArithmetic(String command){
        this.outputFile.write("// "+command+"\n");
        this.outputFile.write(pointToStackTop); // decStackPointer and point to mem
        if ( command.equals("not") ){
            this.outputFile.write("M=!M\n"); //No need get to D reg and then
            //put it back to stack
        } else if ( command.equals("neg") ) {
            this.outputFile.write("M=-M\n"); //No need get to D reg and then
            //put it back to stack
        } else if( command.equals("eq") ) {
            this.writeConditionalArithmetic("JEQ");
        }else if ( command.equals("lt") ){
            this.writeConditionalArithmetic("JLT");
        } else if ( command.equals("gt") ) {
            this.writeConditionalArithmetic("JGT");
        }
        else{
            this.outputFile.write("D=M\n"+pointToStackTop);
            if (command.equals("add")){
                this.outputFile.write("M=D+M\n");
            } else if (command.equals("sub")) {
                this.outputFile.write("M=M-D\n");
            } else if (command.equals("and")) {
                this.outputFile.write("M=D&M\n");
            }else{
                this.outputFile.write("M=D|M\n");
            }
        }
        this.outputFile.write("@SP\n"+"M=M+1\n");
    }
    public void writePushPop(short c_type, String segment, Integer index){
        String setupAddr;
        if ( segment.equals("constant") ){
            if ( c_type == C_PUSH ) {
                this.outputFile.write("// push "+segment+" "+index+"\n");
                setupAddr = "@" + index.toString() + "\n";
                this.outputFile.write(setupAddr+"D=A\n"+putToStack);
            }
            return;
        }else if( segment.equals("static") ){
            setupAddr = "@"+this.fileName+"."+index.toString()+"\n";
            if ( c_type == C_POP ) {
                this.outputFile.write("// pop "+segment+" "+index+"\n");
                this.outputFile.write(pointToStackTop+"D=M\n"+setupAddr+"M=D\n");;
                return;
            }

        }else if ( segment.equals("local") ) {
            setupAddr = "@LCL\n"+"D=M\n"+("@"+index.toString()+"\n")+"AD=A+D\n";
        } else if ( segment.equals("argument") ) {
            setupAddr = "@ARG\n"+"D=M\n"+("@"+index.toString()+"\n")+"AD=A+D\n";
        } else if ( segment.equals("this") ) {
            setupAddr = "@THIS\n"+"D=M\n"+("@"+index.toString()+"\n")+"AD=A+D\n";
        } else if ( segment.equals("that") ) {
            setupAddr = "@THAT\n"+"D=M\n"+("@"+index.toString()+"\n")+"AD=A+D\n";
        } else if ( segment.equals("temp") ) {
            setupAddr = "@5\n"+"D=A\n"+("@"+index.toString()+"\n")+"AD=A+D\n";
        } else if ( segment.equals("pointer") ){
            setupAddr = (index == 0 ? "@THIS\n" : "@THAT\n");
            if ( c_type == C_POP ) {
                this.outputFile.write("// pop "+segment+" "+index+"\n");
                this.outputFile.write(pointToStackTop+"D=M\n"+setupAddr+"M=D\n");
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
            this.outputFile.write(setupAddr+"D=M\n"+putToStack);
        }else{
            this.outputFile.write("// pop "+segment+" "+index+"\n");
            this.outputFile.write(setupAddr+writeToR13+pointToStackTop+"D=M\n"+pointToAddrInR13+
                    "M=D\n");
        }
    }


    public void close(){
        this.outputFile.close();
    }
}
