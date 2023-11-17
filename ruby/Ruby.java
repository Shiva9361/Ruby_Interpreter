package ruby;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
/*
 * The main entry class of the interpreter
 */
public class Ruby {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    /*
     * The entry point of the program 
     * Gets arguments passed in command line 
     * and acts accordingly
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: JRuby [script]");
            // A non-zero status code typically indicates an error condition.
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }
    /*
     * Wrapper method to interpret code from a file
     * Internally reads the data from the file and calls the run method
     */
    // Running the entire file
    private static void runFile(String path) throws IOException {
        // Path.get(path) - converts path string to path object
        byte[] bytes = Files.readAllBytes(Paths.get(path));

        // Charset.defaultCharset() - default character encoding
        run(new String(bytes, Charset.defaultCharset()) + "\n");
        if (hadError)
            System.exit(65);
        if (hadRuntimeError)
            System.exit(70);
    }
    /*
     * Wrapper method to interpret code in interactive mode
     * Internally reads a given line and calls the run method
     */
    // Running line by line
    private static void runPrompt() throws IOException {

        /*
         * This line creates an InputStreamReader object named input that reads input
         * from the standard input stream, typically the keyboard (System.in).
         */
        InputStreamReader input = new InputStreamReader(System.in);

        /*
         * This line creates a BufferedReader named reader, which is wrapped around
         * the input stream. This BufferedReader is used to efficiently read lines of
         * text from the standard input.
         */
        BufferedReader reader = new BufferedReader(input);
        while (true) {
            System.out.print("$");
            String line = reader.readLine();
            if (line == null)
                break;
            // Manualy add a newline
            run(line + "\n");
            //So that the interactive shell goes on 
            hadError = false;
        }
    }
    /*
     * This is the method that actually executes the code
     * Uses the scanner class to generate all tokens
     * Now these tokens are passed to the parser 
     * To generate a parse tree
     * This parse tree is then finally given to interpreter. 
     */
    private static void run(String source) {
        Scanner sc = new Scanner(source);
        List<Token> tokens = sc.scanTokens();
        // Uncomment to check tokens generated
        /*for (Token token : tokens) {
            System.out.println(token);
        }*/
        //System.out.println("-------------");
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        // Stop if there was a syntax error.
        if (hadError)
            return;
        interpreter.interpret(statements);
        // System.out.println(new AstPrinter().print(expression));
    }

    /*
     * Basic Error Handling 
     */
    static void error(int line, String message) {
        report(line, "", message);
    }
    /*
     * Method to report error 
     * And prevents the program from interpreting further
     */
    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
    /*
     * Basic error report
     */
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
    /*
     * Method for runtime error
     */
    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}