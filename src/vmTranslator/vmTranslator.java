package vmTranslator;

public class vmTranslator extends C_TYPES{
    public static void main(String[] args) {
        Parser parser = new Parser(args[1]);
        while (parser.hasNextCommand()){
            short c_type = parser.commandType();
            if ( c_type == C_ARITHMATIC ){

            }else{
                
            }
        }
    }
}
