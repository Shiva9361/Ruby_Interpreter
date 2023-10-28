package ruby;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.TemporalAdjuster;
import java.util.List;

public class Ruby {
    static boolean hadError = false;
    public static void main(String[] args) throws IOException {
        if (args.length>1){
            System.out.println("Usage: JRuby [script]");
            //A non-zero status code typically indicates an error condition.
            System.exit(64);
        }
        else if (args.length ==1){
            runFile(args[0]);
        }
        else{
            runPrompt();
        }
    }
    

    //Running the entire file
    private static void runFile(String path) throws IOException{
        //Path.get(path) - converts path string to path object
        byte[] bytes = Files.readAllBytes(Paths.get(path));

        //Charset.defaultCharset() - default character encoding 
        run(new String (bytes,Charset.defaultCharset()));
        if (hadError) System.exit(65);
    }

    //Running line by line
    private static void runPrompt() throws IOException{

        /*This line creates an InputStreamReader object named input that reads input 
        from the standard input stream, typically the keyboard (System.in).*/
        InputStreamReader input = new InputStreamReader(System.in);

        /*This line creates a BufferedReader named reader, which is wrapped around
        the input stream. This BufferedReader is used to efficiently read lines of text from the standard input. */
        BufferedReader reader = new BufferedReader(input);
        while (true) {
            System.out.print("$");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }
    private static void run(String source){
        Scanner sc = new Scanner(source);
        List<Token> tokens = sc.scanTokens();

        for (Token token :tokens){
            System.out.println(token);
        }
    }

    //Error Handling
    static void error(int line, String message){
        report(line,"",message);
    }

    private static void report(int line, String where,String message){
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}