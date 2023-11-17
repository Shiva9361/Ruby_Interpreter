# Ruby_Interpreter
*A barebone Ruby Interpreter implemented in JAVA*

## **Dependencies:**  
1. Java SDK

## **Features:**
1. Arithmetic Expression Evaluation
2. Operators
    - Arithmetic Operators
        - Addition with String Concatination (+) 
        - Subtraction (-)
        - Multiplication with String Replication (*)
        - Division with floating point and integer(/) 
        - Exponentiation (**) 
    - Logical Operators
        - and (and, &&)
        - or (or, ||)
3. Variable Assignment [^1]
4. Parallel Assignment **(a=b=1)**
5. Print Statement
6. Puts Statement
7. Global Variables
8. Constant Variables
9. Control Statement
    - If Statement
    - If-Else Statement
    - If-Elsif Statement
    - Unless Statement
    - Unless-Else Statement
10. Looping Statements
    - While Statement
    - Until Statement
    - Loop Statement
    - For Statement without step [^2]
11. Loop Control Statements
    - Break Statement
    - Next Statement
12. Functions Without Closures [^3]
13. Comments 
    - Single-line Comments
    - Multi-line Comments using =begin and =end

## **Explanation:**

:warning: *This is a abstract explanation, for further details, check the code for comments* :warning: 


*First, we read the file or the data fed into the interpreter, now this data is passed to the scanner*
*The raw source file is now tokenized in the scanner, The scanner returns a list of tokens* 
*The tokens are now passed to the parser, The parser reads the tokens, and systematically builds a syntax tree* 
*from the ground up using the recursive decent style parsing*

*Now the generated statements/expressions are then finally passed on to the interpreter for the final*
*interpretation. Now the interpreter in short uses the visitor method to find the type of each statement*
*and expression, It then uses the declared methods to interpret the syntax tree and execute the instruction*
*in Java*


## **Challenges:**

1. Absence of semicolon
2. Parallel Assignment
3. Scope issues [^4]
4. Closure issues
5. 0 arguments passing in functions
6. too much alternatives to do the same thing in Ruby
7. Exponent with negative power
8. figuring out how to implement an iterator for 'for' loop

## **Learning:**

1. BNF usage
2. Lexical analysis
    - learnt to differntiate between differnt lexemes
    - learnt to create tokens from source code
3. Parsing
    - learnt recursive decent parsing style
    - learnt to generate parse tree
    - learnt how the type data of one language can be implemented in another language that is interpreting it
    - learnt to implement operator precedence
4. Interpretation
    - learnt to use visitor pattern
    - learnt how functions can be handled 
    - learnt how a interpreter works in the backend

[^1]: variable declaration is not possible in Ruby.
[^2]: Step uses method of number object and as such has not been implemented
[^3]: yield statement is not implemented
[^4]: control flow statements like break and next