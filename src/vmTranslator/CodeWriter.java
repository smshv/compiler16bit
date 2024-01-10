package vmTranslator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Stack;

public class CodeWriter extends C_TYPES{
    private String fileName;
    private final PrintWriter outputFile;
    private int trueLabelNum;
    private int endLabelNum;
    static String putToStack = """
            @SP
            A=M
            M=D
            @SP
            M=M+1
            """;
    static String pointToStackTop = """
            @SP
            AM=M-1
            """;
    static String writeToReg = "@%s\n" +   //R13 is used for storing pointer
            "M=D\n";
    static String pointToAddrInReg = """
            @%s
            A=M
            """;
    static class FuncProp{
        public String funcName;
        public int retCallCount;
        public int numBranches;
        public FuncProp(String funcName){
            this.funcName = funcName;
            this.retCallCount = 0;
            this.numBranches = 0;
        }
    }
    private final Stack<FuncProp> funcStack;
    public CodeWriter(String outputFileName) throws IOException {
        this.outputFile = new PrintWriter( new FileWriter(outputFileName, false) );
        this.trueLabelNum = 0;
        this.endLabelNum = 0;
        this.funcStack = new Stack<FuncProp>();
    }
    public void setFileName(String fileName){
        this.fileName = fileName;
        while ( !funcStack.isEmpty() ){
            funcStack.pop();
        }
        //funcStack.push(fileName); //this allows to add labels belonging to a class. But I think it is not needed
    }
    //private void initSegment(String segmentPointer, int pointerVal){
    //    this.outputFile.write(String.format("@%d\nD=A\n@%s\nM=D\n", pointerVal, segmentPointer));
    //}
    public void writeInit(){
        funcStack.push(new FuncProp("Sys.init"));
        this.outputFile.write("//calling sys.init\n");
        this.outputFile.write("@256\nD=A\n@SP\nM=D\n");
        //this.outputFile.write("@Sys.init\n0;JMP\n");
        writeCall("Sys.init", 0);
    }
    public void writeLabel(String label){
        //this.outputFile.write("//add label\n");
        funcStack.peek().numBranches -= 1; // decrement means branch has been discovered but to be visited in the future. Its like debt
        this.outputFile.write(String.format("(%s$%s)\n", funcStack.peek().funcName,label)); //we reach the end of a function when debt is paid
    }
    public void writeGoTo(String label){
        funcStack.peek().numBranches += 1; //increment means we have visited a branch
        this.outputFile.write(String.format("//Goto %s\n", label));
        this.outputFile.write(String.format("@%s$%s\n0;JMP\n", funcStack.peek().funcName, label));
    }
    public void writeIf(String label){
        funcStack.peek().numBranches += 1;
        this.outputFile.write(String.format("//If-goto %s\n", label));
        this.outputFile.write(pointToStackTop+String.format("D=M\n@%s$%s\nD;JNE\n", funcStack.peek().funcName, label));
    }
    public void writeFunction(String functionName, int numVars){
        funcStack.push(new FuncProp(functionName));
        this.outputFile.write(String.format("//write function %s\n",functionName));
        this.outputFile.write(String.format("(%s)\n", functionName));
        if ( numVars > 0 ) {
            this.outputFile.write("//Initialize local variables\n");
            this.outputFile.write("@SP\n");
            for (int i = 0; i < numVars; i++) {
                this.outputFile.write("A=M\nM=0\n@SP\nM=M+1\n");
            }
            this.outputFile.write("//Local variables initialized\n");
        }
    }
    public void writeCall(String functionName, int numArgs){
        this.outputFile.write("//Preparing to call "+functionName+"\n");
        String retLabel = String.format("ret.%d", funcStack.peek().retCallCount);
        funcStack.peek().retCallCount += 1;
        // save the frame of the caller
        this.outputFile.write(String.format("@%s$%s\n", funcStack.peek().funcName, retLabel));
        this.outputFile.write("D=A\n"+putToStack); //save return address
        this.outputFile.write("@LCL\nD=M\n"+putToStack); //save LCL
        this.outputFile.write("@ARG\nD=M\n"+putToStack); //save ARG
        this.outputFile.write("@THIS\nD=M\n"+putToStack); //save THIS
        this.outputFile.write("@THAT\nD=M\n"+putToStack); //save THAT
        this.outputFile.write(String.format("@SP\nD=M\n@%d\nD=D-A\n", (numArgs+5))); //Compute new address for ARG pointer
        this.outputFile.write("@ARG\nM=D\n"); //Point ARG to new address
        this.outputFile.write("@SP\nD=M\n@LCL\nM=D\n"); //Point LCL to its new address
        // Now new function can start executing
        this.outputFile.write("//Preparation complete. Calling "+functionName+"\n");
        this.outputFile.write(String.format("@%s\n0;JMP\n", functionName));
        writeLabel(retLabel);
    }
    public void writeReturn(){
        if ( funcStack.peek().numBranches == 0 )funcStack.pop(); // if all branches are visited, we have reached the end of the function
        this.outputFile.write("//Preparing to return to the caller\n");
        this.outputFile.write("@LCL\nD=M\n"+String.format(writeToReg, "R14")); //save current LCL->endFrame
        this.outputFile.write("@5\nA=D-A\nD=M\n"+String.format(writeToReg, "R15")); //save return address
        this.outputFile.write(pointToStackTop+"D=M\n"+String.format(pointToAddrInReg, "ARG")+"M=D\n"); //push return value to ARG0
        this.outputFile.write("@ARG\nD=M+1\n@SP\nM=D\n"); //reposition stack pointer for caller
        this.outputFile.write("@R14\nAM=M-1\nD=M\n@THAT\nM=D\n"); //reposition THAT
        this.outputFile.write("@R14\nAM=M-1\nD=M\n@THIS\nM=D\n"); //reposition THIS
        this.outputFile.write("@R14\nAM=M-1\nD=M\n@ARG\nM=D\n"); //reposition ARG
        this.outputFile.write("@R14\nAM=M-1\nD=M\n@LCL\nM=D\n"); //reposition LCL
        this.outputFile.write("//Preparation complete. Returning to the caller\n");
        this.outputFile.write("@R15\nA=M\n0;JMP\n"); //jump to caller
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
                String.format("(%s)\n",trueLabel)+"D=1\n"+String.format("(%s)\n", endLabel));
        this.outputFile.write("""
                @SP
                A=M
                M=D
                """);

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
            }else{ //must be logical or
                this.outputFile.write("M=D|M\n");
            }
        }
        this.outputFile.write("""
                @SP
                M=M+1
                """);
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
                this.outputFile.write(pointToStackTop+"D=M\n"+setupAddr+"M=D\n");
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
            this.outputFile.write(setupAddr+String.format(writeToReg, "R13")+pointToStackTop+"D=M\n"+
                    String.format(pointToAddrInReg, "R13")+
                    "M=D\n");
        }
    }


    public void close(){
        this.outputFile.close();
    }
}
