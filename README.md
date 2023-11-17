# Ruby_Interpreter
An barebone Ruby Interpreter implemented in JAVA

## **Dependencies:**  
1. Java SDK

## **Features:**
1. Arithmetic Expression Evaluation
2. Operators
    1. Arithmetic Operators
        1. Addition (+)(Both String and number)
        2. Subtraction (-)
        3. Multiplication with String Replication (*)
        4. Exponentiation (**) 
    2. Logical Operators
3. Variable Assignment [^1]
4. Parallel Assignment **(a=b=1)**
5. Print Statement
6. Puts Statement
7. Global Variables
8. Constant Variables
9. Control Statement
    1. If Statement
    2. If-Else Statement
    3. If-Elsif Statement
    4. Unless Statement
    5. Unless-Else Statement
10. Looping Statements
    1. While Statement
    2. Until Statement
    3. Loop Statement
    4. For Statement without step [^2]
11. Loop Control Statements
    1. Break Statement
    2. Next Statement
12. Functions Without Closures [^3]
13. Comments 
    1. Single-line Comments
    2. Multi-line Comments using =begin and =end

## **Explanation:**

::warn:: This is a high level explanation, for further details, check the code for comments
First, we read the file or the data fed into the interpreter


## **Challenges:**

1. Absence of semicolon
2. Parallel Assignment
3. 

## **Learning:**

1. Lexical analysis
    - learnt to differntiate between differnt lexemes
    - learnt to create tokens from source code
2. Parsing
    - learnt recursive decent parsing style
    - learnt to generate parse tree
    - learnt how the type data of one language can be implemented in another language that is interpreting it
    - learnt to implement operator precedence

3. Interpretation
    - learnt to use visitor pattern

[^1]: variable declaration is not possible in Ruby.
[^2]: Step uses method of number class and as such has not been implemented
[^3]: yield statement is not implemented