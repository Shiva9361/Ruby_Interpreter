package ruby;

import static ruby.TokenType.NIL;
// this class is used to throw runtime errors 
public class RuntimeError extends RuntimeException {
    final Token token;
    //Parameterized constructor for runtime error
    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
    // If we don't have a specific token information 
    // Then throw this error
    RuntimeError(String message) {
        super(message);
        this.token = new Token(NIL, "", "", 0);
    }
}
