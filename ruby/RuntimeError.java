package ruby;

import static ruby.TokenType.NIL;

public class RuntimeError extends RuntimeException {
    final Token token;

    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }

    RuntimeError(String message) {
        super(message);
        this.token = new Token(NIL, "", "", 0);
    }
}
