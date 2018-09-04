// Packages
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PushbackInputStream;

//These two class references greatly helped throughout:
//- https://docs.oracle.com/javase/7/docs/api/java/io/PushbackInputStream.html
//- https://docs.oracle.com/javase/7/docs/api/java/io/FileInputStream.html
//- As well as other references from - https://docs.oracle.com/javase/7/docs/api/java/io/InputStream.html
public class Tokenizer {

/*
  Methods:
    GetNextToken()
    PeekNextToken()

    GetNextChar()
    PeekNextChar()

    IdentifyToken()

    One function per token rule
      Identifier := ( _ | Alpha ) { ( _ | Digit | Alpha ) }
      IntConstant := [ - ] Digit { Digit }
      FloatConstant := [ - ] Digit { Digit } [ . Digit { Digit } ]
      Digit := 0 – 9
      Alpha := A – Z | a - z
      Operators
        RelationOperator := ( == ) | < | > | ( <= ) | ( >= ) | ( != )
        AddOperator := + | -
        MultOperator := * | /


  Variables:
    int RowCount
    int ColCount
    string TokenType
          - Operator
          - intConstant
          - floatConstant
          - Keyword
          - Identifier
    string TokenRead

    FileInputStream fis
    the file itself


*/

  // "GLOBAL"
  static PushbackInputStream fis_char;
  static int rowNum; //increment whenever we have new line--read "\n"
  static int colNum; //increment whenever we read ANYTHING in a line but RESET when \n, then inc rowNumber
  static int tokColNum;
  static boolean lastTokWasIDOrConst;

  static Token t;
  static boolean debug;

  //Dummy constructor
  public Tokenizer(String fileName) {
    lastTokWasIDOrConst = false;
    rowNum = 1;
    colNum = 1;
    tokColNum = 1;

    //Open the file
    try{fis_char = new PushbackInputStream(new FileInputStream(fileName), 100);} catch(FileNotFoundException e){System.out.println("oops!"); System.exit(1);}
  }


  public static char GetNextChar(){
    char c=0;
    try{ c = (char) fis_char.read();} catch(IOException e){System.out.print("fail read");}

    if(c == '\n') {
      rowNum++;
      colNum = 1;
    }
    else {
      colNum++;
      if(debug)System.out.println("******  colNum: "+colNum);
    }
    return c;
  }


  public static int PeekNextChar() {
    int c=0;
    //read and immediately unread
    try{c =  fis_char.read();}catch(IOException e){System.out.println("fail read");}
    try{fis_char.unread(c);}catch(IOException e){System.out.println("fail unread");}
    return c;
  }


  //This function wraps around the real get next token function.
  public static Token GetNextToken() {
    Token token = GNT();
    fis_char.mark(1000);
    if(debug)System.out.println("LENGTH OF VALUE: "+token.value.length());
    if(debug)System.out.println("COL NUM: "+colNum);

    if(colNum <=0) {
      System.out.println("bad col num");
      System.exit(1);
    }

    return token;
  } // getNextToken()

  public static Token PeekNextToken() {
    fis_char.mark(1000);
    int oldColNum = colNum;
    int oldRowNum = rowNum;

    Token token = GNT();

    colNum = oldColNum;
    rowNum = oldRowNum;
    try{fis_char.unread(token.value.getBytes());}catch(IOException e){System.out.println("fail unread "+e.toString());}

    return token;
  } // getNextToken()

  public static boolean isWhitespace(char c) {
    if(c==' ' || c == '\n')
      return true;

    return false;
  } // isWhitespace()

  // Please note that SOME CHARACTERS ARE NOT OPERATORS ON THEIR OWN
  public static boolean isOperator(String c) {
    String c_array[] = {"(", ",", ")", "{", "}", "=", "==", "<", ">", "<=", ">=", "!", "!=", "+", "-", "*", "/", ";"};
    for(int i = 0; i < c_array.length; i++) {
      if(c.compareTo(c_array[i]) == 0)
        return true;
    }
    return false;
  } // isOperator()

  // This function is wrapped within GetNextToken().
  public static Token GNT() {
    StringBuffer result = new StringBuffer("");
    String type = new String("");
    boolean hasNegativeSign = false;
    int oldColNum = colNum;

    int c = PeekNextChar();

    while(c != -1 && c != 255) {

      if(debug)System.out.println("RESULT IS CURRENTLY: "+result);
      if(debug)System.out.println("TYPE IS CURRENTLY: "+type);
      if(debug)System.out.println("CHAR IS CURRENTLY: '"+(char)c+"' ");

      //Case Zero: We hit whitespace.  That's a ' ' or '\n'.
      while(isWhitespace((char)c)) {
        if(debug)System.out.println( "    is whitespace");

        //If result is whitespace BUT result is "", discard whitespace and keep going
        if(result.toString().compareTo("") == 0) {
          if(debug)System.out.println("    discard whitespace");
          if(debug)System.out.println("    keep going");

          GetNextChar();
          c = PeekNextChar();
          if(debug)System.out.println("Next char: '"+(char)c+"' ");
        }

        // Otherwise, we have reached the end of the token.  Return current token.
        else {
          if(debug)System.out.println("    return the string :DDDD");
          GetNextChar();
          c = PeekNextChar();
          if(debug)System.out.println("Next char: '"+(char)c+"' ");

          return new Token(result.toString(), type, rowNum, tokColNum);
        }
      } // while next is whitespace

      tokColNum = colNum;
      if(debug)System.out.println("tok's colNum: "+tokColNum+" ");

      // Eliminate the possibility that we have hit the end of the file
      // after whitespace
      if(c == -1 || c == 255) {
        type = "None";
        return new Token(result.toString(), type, rowNum, tokColNum);
      }

      //Case One: We hit an "operator."
      if(isOperator(""+(char)c)) {
        if(debug)System.out.println( "    is \"operator\"");

        //We have a potential problem: if c is a '-', this could be a minus sign.
        if((char)c == '-') {
          // If the last token was NOT an ID or const, the current token-in-progress
          // may be a negated constant.
          if(!lastTokWasIDOrConst) {
            if(debug)System.out.println("    last token was NOT ID or const");
            if(debug)System.out.println("    this token MIGHT BE a negated const");
            if(debug)System.out.println("    append and keep going");

            result.append((char)c);
            hasNegativeSign = true;
            lastTokWasIDOrConst = false;

            GetNextChar();
            c = PeekNextChar();
            if(debug)System.out.println("Next char: '"+(char)c+"' ");

            //Check what c is.
            // If c is another operator or whitespace, then the current result is an operator.
            // Return current result, but do not consume next character.
            if(isOperator(""+(char)c) || isWhitespace((char)c)) {
              type = "Operator";
              lastTokWasIDOrConst = false;
              return new Token(result.toString(), type, rowNum, tokColNum);
            }
          }

          //Else, if the last token WAS an ID or const, the current token-in-progress
          // is an operator.
          else {
            if(isOperator(""+(char)c)) {
              if(debug)System.out.println("    last token WAS ID or const");
              type = "Operator";
              lastTokWasIDOrConst = false;
              result.append((char)c);

              GetNextChar();
              c = PeekNextChar();
              if(debug)System.out.println("Next char: '"+(char)c+"' ");

              return new Token(result.toString(), type, rowNum, tokColNum);
            }
          }
        }

        // Previous token was not an operator
        // c is a (or is part of a) true operator
        // Legal operators: ( , ) { } = == < > <= >= != + - * / ;
        // Result should be empty at this point.
        else {
          //There is at least one character in the operator.
          // Append first operator character.
          if(debug)System.out.println("    truly an operator");
          if(debug)System.out.println("    SET type to OPERATOR");
          if(debug)System.out.println("    append operator to empty result");

          type = "Operator";
          lastTokWasIDOrConst = false;
          result.append((char)c);

          GetNextChar();
          c = PeekNextChar();

          if(debug)System.out.println("Next char: '"+(char)c+"' ");

          // Check for second char in operator
          if(isOperator(result.toString()+(char)c)) {// If result+c is an operator
            if(debug)System.out.println("    result+c is an operator");
            if(debug)System.out.println("    return the string");

            result.append((char)c);
            GetNextChar();

            return new Token(result.toString(), type, rowNum, tokColNum);
          }

          // c is some identifier/const right before the operator
          // Return result BUT DO NOT skip over c
          else {
            if(debug)System.out.println("    return the string");

            return new Token(result.toString(), type, rowNum, tokColNum);
          }
        } // lastTokenWas NOT an operator
      } // if next is operator

      //Case Two: token-in-progress isn't whitespace or an operator
      //(or negative sign, that's been dealt with).
      //The way this is set up, result should still be empty (or only have a negative sign)
      //Current token may be an Identifier or a constant.

      // If c is '_' or a legal character, token may be an identifier
      // If this if statement is true, result should be empty.
      if((char)c == '_' || isAlpha(c)) {
        if(debug)System.out.println("    c is '"+(char)c+"'");
        if(debug)System.out.println("    append c to result (first char)");
        if(debug)System.out.println("    result is currently "+result);

        result.append((char)c);

        GetNextChar();
        c = PeekNextChar();
        if(debug)System.out.println("Next char: '"+(char)c+"' ");

        // Check if suceeding characters in stream are part of identifier token
        while((char)c == '_' || isAlpha(c) || isDigit(c)) {
          if(debug)System.out.println("    c is '"+(char)c+"'");
          if(debug)System.out.println("    append c to result (building)");

          result.append((char)c);

          GetNextChar();
          c = PeekNextChar();
          if(debug)System.out.println("Next char: '"+(char)c+"' ");
        } // while

        // At this point, we have a legal ID.
        // Return the legal ID.
        if(debug)System.out.println("    result is truly a IDENTIFIER");
        if(debug)System.out.println("    return the string");

        type = setIdentifierType(result.toString());
        lastTokWasIDOrConst = true;
        return new Token(result.toString(), type, rowNum, tokColNum);
      } // if c is an underscore or alpha and thus PERHAPS AN IDENTIFIER

      // At this point, the only other option for this token to be is a const.
      // There may be a minus sign already sitting in result, but that does
      //   not matter at this point.
      if(hasNegativeSign) {
        if(debug)System.out.println("    has negative sign");
      }

      // Check the first character against number rules.
      // First char must be a digit.
      if(isDigit(c)) {
        if(debug)System.out.println("    "+(char)c+" is a digit");
        if(debug)System.out.println("    append c to result");
        result.append((char)c);

        GetNextChar();
        c = PeekNextChar();
        if(debug)System.out.println("Next char: '"+(char)c+"' ");
      }

      // There wasn't at least one digit.  Return invalid token.
      else {
        if(debug)System.out.println("    need at least one digit");
        if(debug)System.out.println("    return invalid string "+c);

        //Check that we aren't at end of file
        if(c == -1 || c == 255) {
          type = "None";
        }
        else {
          type = "Invalid";
        }
        result.append((char)c);
        GetNextChar();
        lastTokWasIDOrConst = false;
        return new Token(result.toString(), type, rowNum, tokColNum);
      }

      // Find and append as many digits as possible.
      while(isDigit(c)) {
        if(debug)System.out.println("    "+(char)c+" is a digit");
        if(debug)System.out.println("    append c to result");
        result.append((char)c);

        GetNextChar();
        c = PeekNextChar();
        if(debug)System.out.println("Next char: '"+(char)c+"' ");
      } // inner while

      if(debug)System.out.println("    result is currently "+result);

      // We have either hit an illegal char for a const, or a decimal

      // If c is NOT a '.' then return token as an int constant.
      if((char)c != '.') {
        if(debug)System.out.println("    result is truly a INT CONSTANT");
        if(debug)System.out.println("    return the string");

        type = "IntConstant";
        lastTokWasIDOrConst = true;
        return new Token(result.toString(), type, rowNum, tokColNum);
      } // if c is NOT a decimal point

      // If c IS a '.' then check if there is at least one digit after the '.'
      else {
        if(debug)System.out.println("    c is a decimal - check if float");

        result.append((char)c);

        GetNextChar();
        c = PeekNextChar();
        if(debug)System.out.println("Next char: '"+(char)c+"' ");

        // Check that there's at least one digit after the '.'
        if(isDigit(c)) {
          if(debug)System.out.println("    "+(char)c+" is a digit");
          if(debug)System.out.println("    append c to result yyyy");

          result.append((char)c);

          GetNextChar();
          c = PeekNextChar();
          if(debug)System.out.println("Next char: '"+(char)c+"' ");
        } // inner while

        // There wasn't at least one digit.  Return invalid token.
        else {
          if(debug)System.out.println("    need at least one digit");
          if(debug)System.out.println("    return invalid string "+c);

          //Check that we aren't at end of file
          if(c == -1 || c == 255) {
            type = "None";
          }
          else {
            type = "Invalid";
          }

          //c = NextChar();
          lastTokWasIDOrConst = false;
          return new Token(result.toString(), type, rowNum, tokColNum);
        }

        // Find and append as many digits as possible.
        while(isDigit(c)) {
          if(debug)System.out.println("    "+(char)c+" is a digit");
          if(debug)System.out.println("    append c to result");
          result.append((char)c);

          GetNextChar();
          c = PeekNextChar();
          if(debug)System.out.println("Next char: '"+(char)c+"' ");
        } // inner while

        // Check that the next character won't make this const illegal.
        // i.e. the next character MUST be a whitespace or an operator.
        if(isWhitespace((char)c) || isOperator(""+(char)c)) {
          if(debug)System.out.println("    result is truly a FLOAT CONST");
          if(debug)System.out.println("    return the string");

          type = "FloatConstant";
          lastTokWasIDOrConst = true;
          return new Token(result.toString(), type, rowNum, tokColNum);
        } // if is whitespace or operator

        // The next character made this const illegal.  Return an invalid token.
        else { //noooooo it's invalid :(
          if(debug)System.out.println("    next char '"+(char)c+"' makes result INVALID");
          if(debug)System.out.println("    return the string");

          result.append((char)c);
          GetNextChar();
          type = "Invalid";
          lastTokWasIDOrConst = false;
          return new Token(result.toString(), type, rowNum, tokColNum);
        } // else invalid
      } // c is a decimal point
    } // while

    // We should never reach this point unless we hit the end of the file.
    if(result.toString().compareTo("") == 0)
      type = "None";

    // One-off correction to tokColNum b/c we never call getNextChar()
    return new Token(result.toString(), type, rowNum, tokColNum+1);
  } //PeekNextToken()


  public static String IdentifyToken(String tok){
    //Case the Zeroth: tok is an operator
    if(isOperator(tok))
      return new String("Operator");

    String[] Keywords = {"float", "int", "void", "main", "unsigned", "char", "short", "int", "long", "float", "double", "while", "if", "return"};
    for (int i = 0; i < Keywords.length; i++) {
      if (tok.compareTo( Keywords[i]) == 0) {
        return new String("Keyword");
      }
    } // for

      return "dummy answer";
  }

  //General ASCII table reference:
  //   http://www.asciitable.com

  public static boolean isAlpha(int c) {
    return ((c >=65 && c <=90 ) || (c >=97 && c <=122));
  }


  public static boolean isDigit(int c) {
    return (c >=48 && c <=57 );
  }


  public static String setIdentifierType(String id) {
    String[] Keywords = {"float", "int", "void", "main", "unsigned", "char", "short", "int", "long", "float", "double", "while", "if", "return"};
    for (int i = 0; i < Keywords.length; i++) {
      if (id.compareTo( Keywords[i]) == 0) {
        return new String("Keyword");
      } // if
    } // for
   return "Identifier";
  } // setIdentifierType();


  public static void main(String[] args) {
    if(args.length == 0) {
        System.out.println("no args specified");
        System.exit(1);
      }

    if(args[0].compareTo("") == 0) {
      System.out.println("no file specified");
      System.exit(1);
      }

    if(args.length < 2 || args[1].compareTo("") == 0) {
      debug = false;
    }
    else {
      debug = true;
    }
    if(debug)System.out.println("debug is "+debug);

    //Initialize Tokenizer (and all static variables).
    Tokenizer tz = new Tokenizer(args[0]);

    Token temp;
    do {
      temp = GetNextToken();
      System.out.println(temp.toString());
    } while(temp.value.compareTo("") != 0);

  } // main
} //end of class
