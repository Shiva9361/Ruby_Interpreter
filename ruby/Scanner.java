package ruby;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static ruby.TokenType.*;

public class Scanner {

    private static final Map<String, TokenType> keywords;
    /*
     * Creating a map for all keywods found in ruby that we are implenenting
     * If a keyword is encountered, It is added to the token
     * instead of setting as identifier
     */
    static {
        keywords = new HashMap<>();
        keywords.put("BEGIN", BEGIN_C);
        keywords.put("END", END_C);
        keywords.put("and", AND);
        keywords.put("begin", BEGIN);
        keywords.put("break", BREAK);
        keywords.put("case", CASE);
        keywords.put("class", CLASS);
        keywords.put("def", DEF);
        keywords.put("do", DO);
        keywords.put("else", ELSE);
        keywords.put("elsif", ELSIF);
        keywords.put("end", END);
        keywords.put("ensure", ENSURE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("if", IF);
        keywords.put("in", IN);
        keywords.put("module", MODULE);
        keywords.put("next", NEXT);
        keywords.put("nil", NIL);
        keywords.put("not", NOT);
        keywords.put("or", OR);
        keywords.put("rescue", RESCUE);
        keywords.put("retry", RETRY);
        keywords.put("return", RETURN);
        keywords.put("self", SELF);
        keywords.put("super", SUPER);
        keywords.put("then", THEN);
        keywords.put("true", TRUE);
        keywords.put("unless", UNLESS);
        keywords.put("when", WHEN);
        keywords.put("while", WHILE);
        keywords.put("print", PRINT);
        keywords.put("puts", PUTS);
        keywords.put("loop", LOOP);
        keywords.put("until", UNTIL);
        keywords.put("break", BREAK);
        keywords.put("redo", REDO);
        // keywords.put("yield");

    }

    /*
     * We don't want to ever change the source during execution
     * Also we don't want to create a new token list
     * So both are final
     */
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    // Parameterized constructor
    /*
     * Scanner reads a source file or line
     */
    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // beginning of next lex
            // After geerating one token, change start to current
            // Which is the start of next lexeme
            start = current;
            scanToken();
        }
        // adding EOF to the arraylist of tokens by creating a new Token object
        // So the last token will be EOF
        tokens.add(new Token(EOF, "", null, line));
        return tokens; // Tokens is returned for the parser to run
    }

    // Recognizing Lexemes
    private void scanToken() {
        char c = advance();
        /*
         * The switch case creates token based on lexeme encountered
         */
        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case '[':
                addToken(LEFT_SQUARE);
                break;
            case ']':
                addToken(RIGHT_SQUARE);
                break;

            case ',':
                addToken(COMMA);
                break;
            case '.':
                 if (match('.')) {
                    if(match('.'))
                    {
                        addToken(DOT_DOT_DOT);
                    }
                    else {
                    addToken(DOT_DOT);
                    }
                } 
                 else {
                    addToken(DOT);
                }
                break;
            // we need to look at the second character.
            case '-':
                addToken(match('=') ? MINUS_EQUAL : MINUS);
                break;
            case '+':
                addToken(match('=') ? PLUS_EQUAL : PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '?':
                addToken(QUESTION_MARK);
                break;
            case '%':
                addToken(match('=') ? MOD_EQUAL : MOD);
                break;
            case '*':
                if (match('=')){
                    addToken(STAR_EQUAL);
                }
                else if (match('*')){
                    addToken(STAR_STAR);
                }
                else{
                    addToken(STAR);
                }
                
                break;

            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                if (match('=')){
                    addToken(EQUAL_EQUAL);
                }
                else if (iscomment()){
                    break;
                }
                break;


            case '<':
                if (match('=')) {
                    addToken(LESS_EQUAL);
                } else if (match('<')) {
                    addToken(LEFT_SHIFT);
                } else {
                    addToken(LESS);
                }
                break;
            case '>':
                if (match('=')) {
                    addToken(GREATER_EQUAL);
                } else if (match('>')) {
                    addToken(RIGHT_SHIFT);
                } else {
                    addToken(GREATER);
                }
                break;

            case ':':
                addToken(match(':') ? COLON_COLON : COLON);
                break;
            case '&':
                addToken(match('&') ? AND : AMPERSAND);
                break;
            case '|':
                addToken(match('|') ? OR : PIPE);
                break;
            case '^':
                addToken(KARROT);
                break;
            case '~':
                addToken(TILDA);
                break;
            case '/':
                if (match('=')) {
                    addToken(SLASH_EQUAL);
                } else {
                    addToken(SLASH);
                }
                break;

            // comment in ruby is by #
            case '#':
                while (peek() != '\n' && !isAtEnd())
                    advance();
                if (peek() == '\n')
                    advance();
                line++; // Consuming the newline too

                // Ignoring all kinds of white spaces
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                addToken(NEWLINE); // we need the newline token to find the end of our current line
                line++;
                break;

            // int values
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '0':
                number();
                break;
            // String
            /*
             * Both "s", 's' are strings in ruby
             */
            case '"':
                string();
                break;
            case '\'':
                string2();
                break;

            /*
             * In default we are checking for identifiers
             */
            default:
                if (isAlpha(c)) {
                    identifier();
                    break;
                }
                // if it is not a charcater that we know, throw error
                Ruby.error(line, "Unexpected Character");
                break;
        }
    }

    /*
     * Helper Functions
     */
    // Check if it is at end
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // consumes the next character in the source file and returns it
    private char advance() {
        current++;
        return source.charAt(current - 1);
    }
    /*
     * Operator overloading
     */
    // Adds the token with null object
    private void addToken(TokenType type) {
        addToken(type, null);
    }
    // Adds token with object
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
    // If we find the charater we want, Then increment
    // Else don't
    private boolean match(char expected) {

        // Current is already increamented when advance is called
        // That's why we need to check "CURRENT" now
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;

        current++;
        return true;
    }

    // Dosen't increment current
    // Like Look Ahead
    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }
    /*
     * Method to generate string tokens
     */
    private void string() {
        /*
         * Ruby supports multiline strings...
         */
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line++;
            advance();
        }
        if (isAtEnd()) {
            Ruby.error(line, "Unterminated String");
            return;
        }

        // This is for the closing "
        advance();

        String value = source.substring(start + 1, current - 1);
        /*
         * Java reads the bytes and adds extra slash
         * Soo need to remove
         * \" \\"
         */
        value = value.replace("\\n", "\n");
        value = value.replace("\\t", "\t");
        value = value.replace("\\r", "\r");
        value = value.replace("\\f", "\f");
        // value = value.replace(, "\n");
        value = value.replace("\\'", "\'");
        value = value.replace("\\\\", "\\");
        // System.out.println(value+"scanner");
        addToken(STRING, value);
    }
    /*
     * String with single quotes 
     */
    private void string2() {
        /*
         * Ruby supports multiline strings...
         */
        while (peek() != '\'' && !isAtEnd()) {
            if (peek() == '\n')
                line++;
            advance();
        }
        if (isAtEnd()) {
            Ruby.error(line, "Unterminated String");
            return;
        }

        // This is for the closing '
        advance();

        String value = source.substring(start + 1, current - 1);
        /*
         * Java reads the bytes and adds extra slash
         * Soo need to remove
         * \" \\"
         */
        value = value.replace("\\n", "\n");
        value = value.replace("\\t", "\t");
        value = value.replace("\\r", "\r");
        value = value.replace("\\f", "\f");
        // value = value.replace(, "\n");
        value = value.replace("\\'", "\'");
        value = value.replace("\\\\", "\\");
        // System.out.println(value+"scanner");
        addToken(STRING, value);
    }
    /*
     * method to check if it is number
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    /*
     * method to peek 2 chararacters forward
     */
    private char peekNext() {
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }
    /*
     * Method to check for multiline commments
     */
    private boolean iscomment(){
        int extra = 0;
        while (isAlpha(peek())){
            advance(); extra++;
        }
        int end=0;
        String text = source.substring(start, current);
        if (text.compareTo("=begin")==0){
            while(!isAtEnd()) {
                if (peek() == '\n'){
                    line++;
                }
                if (peek() == '='){
                    end = current;
                    advance();
                    while (isAlpha(peek()))
                    advance();
                    String text2 = source.substring(end, current);
                    if (text2.compareTo("=end")==0){
                        return true;
                    }
                }
                advance();
            }
            Ruby.error(line, "Expect =end");
        }
        else{
            current= current - extra;
            addToken(EQUAL);
            return false;
        }
        return false;
    }
    /*
     * Method to get generate token for number
     */
    private void number() {
        boolean isFloat = false;
        while (isDigit(peek()))
            advance();
        // Not considering method, have to change
        if (peek() == '.' && isDigit(peekNext())) {
            isFloat = true;
            advance();
            while (isDigit(peek()))
                advance();
            // Checking if identifier is named with number
            if (isAlpha(peek())) {
                Ruby.error(line, "Invalid Indentifier");
            }
        }
        if (!isFloat) {
            addToken(INTEGER, Integer.parseInt((source.substring(start, current))));
        }
        // we are using java's double for float in ruby
        else {
            addToken(FLOAT, Double.parseDouble((source.substring(start, current))));
        }

    }

    // after we scan an identifier, we check to see if it matches anything in the
    // map if it does then we add a keyword token 
    // else indentifier
    
    private void identifier() {
        while (isAlphanumeric(peek()))
            advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null)
            type = IDENTIFIER;
        addToken(type);
    }
    /*
     * method to check for alphabets and _
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_' || c == '$';
    }
    /*
     * method to check for alphabets and numeric together
     */
    private boolean isAlphanumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

}
