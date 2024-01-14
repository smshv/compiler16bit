package JackAnalyzer;
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.IntStream;

public class JackTokenizer {
    private final BufferedInputStream inputFile;
    private int lastReadChar;
    public String currentToken;
    private static final int[] SYMBOLS = {'{','}','[',']','(',')','+','-','*','/',
                                            '&','|','~','<','>','=','.',',',';'};
    public JackTokenizer(File file) throws FileNotFoundException {
        this.inputFile = new BufferedInputStream(new FileInputStream(file));
        this.lastReadChar = 0;
        this.currentToken = "";
    }

    public boolean hasMoreTokens() {
        return this.lastReadChar != -1;
    }
    private void readNextChar() throws IOException {
        if ( this.lastReadChar != -1 ) this.lastReadChar = this.inputFile.read();
    }

    private void getNonWhiteChar() throws IOException {
        if ( this.lastReadChar == 0 ) readNextChar();
        while ( this.lastReadChar != -1 && Character.isWhitespace(this.lastReadChar)) {
            readNextChar();
        }
    }
    public void advance() throws IOException { //advance is called only if hasMoreTokens returns true
        getNonWhiteChar();
        StringBuilder str = new StringBuilder();
        while ( this.lastReadChar == '/') {
            readNextChar();
            if (this.lastReadChar == '/') { //ignore single line comments
                readNextChar();
                while (this.lastReadChar != -1 && this.lastReadChar != '\n') {
                    readNextChar();
                }
                readNextChar();
                getNonWhiteChar(); // repeat if more comments
            } else if (this.lastReadChar == '*') { //ignore multi-lines comments
                int prevChar = this.lastReadChar;
                readNextChar();
                while ( this.lastReadChar != - 1 && (prevChar != '*' || this.lastReadChar != '/') ) {
                    prevChar = this.lastReadChar;
                    readNextChar();
                }
                readNextChar();
                getNonWhiteChar(); //repeat if more comments
            }else{
                str.append("/"); // it is / symbol. processed here
            }
        }

        if ( this.lastReadChar != -1 && str.length() == 0){ //if a valid char and it is not / symbol
            //this char is non-white and non-comments
            if ( this.lastReadChar == '"' ){ ////check if it is a string constant
                readNextChar();
                while (this.lastReadChar != -1 && this.lastReadChar != '"' ){ //add every thing until end quote
                    str.append((char) this.lastReadChar);
                    readNextChar();
                }
            }else{ //possibly a symbol or a sequence of alphanumeric chars
                int tmp = this.lastReadChar;
                while ( this.lastReadChar != -1 && (Character.isAlphabetic(this.lastReadChar) ||
                        Character.isDigit(this.lastReadChar) || this.lastReadChar == '_')){ //create a alphanumeric sequence
                    str.append((char) this.lastReadChar);
                    readNextChar();
                }
                if ( str.length() == 0 ){ // if not alphanumeric then a symbol
                    str.append((char) tmp);
                    readNextChar(); //move beyond the current token
                }
            }
            this.currentToken = str.toString();
        }
    }
    public void close() throws IOException {
        this.inputFile.close();
    }
}
