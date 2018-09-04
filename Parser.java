// General Theory: https://en.wikipedia.org/wiki/Recursive_descent_parser
// Also referenced class notes and Piazza.

public class Parser {
  public  static Token currTok;
  public static boolean debug;
  public static Tokenizer tz;

  public Parser(String filename) {
    //constructor
    tz = new Tokenizer(filename);
    currTok = tz.GetNextToken();
  }

  public static void main(String args[]) {
    if(args.length < 1) {
      System.out.println("Need one arg");
      System.exit(1);
    }

    if(args.length == 2) {
      debug = true;
      System.out.println("debug is true");
    }
    else
      debug = false;

    Parser p = new Parser(args[0]);

    p.ParseProgram();

    System.out.println(args[0]+" is a valid X program!");
  } // main()

  /******* Debugging***********/

   public void db(String s) {
    if(debug)System.out.println("In block \""+s+"\"");
    if(debug)System.out.println("Current token: "+currTok.toString());
  }

  public void db1(String s) {
    if(debug)System.out.println("exiting \""+s+"\"");
  }

  /********** End Debugging *********/

  public void consumeToken() {
    if(debug)System.out.println("    CONSUMING "+currTok.toString());
    currTok = tz.GetNextToken();
  }

  public boolean isFirst(Token tok, String rule) {
    if(rule.equals("Program"))
      return isFirst(tok, "Declaration") || isFirst(tok, "MainDeclaration");
    else if(rule.equals("Declaration"))
      return isFirst(tok, "DeclarationType");
    else if(rule.equals("MainDeclaration"))
      return tok.value.equals("void");
    else if(rule.equals("FunctionDefinition"))
      return isFirst(tok, "DeclarationType");
    else if(rule.equals("DeclarationType"))
      return isFirst(tok, "DataType");
    else if(rule.equals("VariableDeclaration"))
      return tok.value.equals(";") || tok.value.equals("="); // may be problem here
    else if(rule.equals("FunctionDeclaration"))
      return isFirst(tok, "ParameterBlock");
    else if(rule.equals("Block"))
      return tok.value.equals("{");
    else if(rule.equals("ParameterBlock"))
      return tok.value.equals("(");
    else if(rule.equals("DataType"))
      return isFirst(tok, "IntegerType") || isFirst(tok, "FloatType");
    else if(rule.equals("Constant"))
      return isFirst(tok, "IntConstant") || isFirst(tok, "FloatConstant");
    else if(rule.equals("Statement"))
      return   isFirst(tok, "Assignment")
            || isFirst(tok, "WhileLoop")
            || isFirst(tok, "IfStatement")
            || isFirst(tok, "ReturnStatement")
            || isFirst(tok, "Expression");
    else if(rule.equals("Parameter"))
      return isFirst(tok, "DataType");
    else if(rule.equals("IntegerType"))
      return   tok.value.equals("unsigned")
            || tok.value.equals("char")
            || tok.value.equals("short")
            || tok.value.equals("int")
            || tok.value.equals("long");
    else if(rule.equals("FloatType"))
      return tok.value.equals("float") || tok.value.equals("double");
    else if(rule.equals("Assignment"))
      return isFirst(tok, "Identifier");
    else if(rule.equals("WhileLoop"))
      return tok.value.equals("while");
    else if(rule.equals("IfStatement"))
      return tok.value.equals("if");
    else if(rule.equals("ReturnStatement"))
      return tok.value.equals("return");
    else if(rule.equals("Expression"))
      return isFirst(tok, "SimpleExpression");
    else if(rule.equals("SimpleExpression"))
      return isFirst(tok, "Term");
    else if(rule.equals("Term"))
      return isFirst(tok, "Factor");
    else if(rule.equals("Factor"))
      return   tok.value.equals("(")
            || isFirst(tok, "Constant")
            || isFirst(tok, "Identifier");
    else if(rule.equals("RelationOperator"))
      return   tok.value.equals("==")
            || tok.value.equals("<")
            || tok.value.equals(">")
            || tok.value.equals("<=")
            || tok.value.equals(">=")
            || tok.value.equals("!=");
    else if(rule.equals("AddOperator"))
      return tok.value.equals("+") || tok.value.equals("-");
    else if(rule.equals("MultOperator"))
      return tok.value.equals("*") || tok.value.equals("/");
    else if(rule.equals("Identifier"))
      return tok.type.equals("Identifier"); //NOTE: this is special.  Type, not value.
    else if(rule.equals("IntConstant"))
      return tok.type.equals("IntConstant"); //NOTE: this is special.  Type, not value.
    else if(rule.equals("FloatConstant"))
      return tok.type.equals("FloatConstant"); //NOTE: this is special.  Type, not value.
    else {
      System.out.println("Rule = \""+rule+"\" isn't valid.");
      return false;
    }
  } // isFirst();

  public void error(String parseBlock, String failedRule) {
    if(debug) {
      System.out.print("ERROR in \""+parseBlock+"\": ");
      if(failedRule.equals(""))
        System.out.println("No rule for the given parse block applied.");
      else
        System.out.println("\""+failedRule+"\"");
      System.exit(1);
    }

    System.out.println("In parsing "+parseBlock+" unexpected token \""+currTok.value+"\" of type "+currTok.type+" on line "+currTok.row_num+".");
    System.exit(1);
  } // error


  public void error(String parseBlock, String failedRule, String token) {
    if(debug) {
      System.out.print("ERROR in \""+parseBlock+"\": ");
      if(failedRule.equals(""))
        System.out.println("No rule for the given parse block applied.");
      else
        System.out.println("\""+failedRule+"\"");
      System.out.println("Token: "+token);
      System.exit(1);
    }

    System.out.println("In parsing "+parseBlock+" unexpected token \""+currTok.value+"\" of type "+currTok.type+" on line "+currTok.row_num+".");
    System.exit(1);
  }


  public void ParseProgram() {
    if(debug)System.out.println(currTok.value);
    while(isFirst(currTok, "Declaration"))
      ParseDeclaration();

    ParseMainDeclaration();

    while(isFirst(currTok, "FunctionDefinition"))
      ParseFunctionDefinition();
  }

  public void ParseDeclaration() {
    db("Declaration");
    ParseDeclarationType();

    if(isFirst(currTok, "VariableDeclaration"))
      ParseVariableDeclaration();
    else if(isFirst(currTok, "FunctionDeclaration"))
      ParseFunctionDeclaration();
    else
      error("Declaration", "", currTok.value);
  }

  public void ParseMainDeclaration() {
    db("MainDeclaration");
    if(currTok.value.equals("void"))
      consumeToken();
    else
      error("MainDeclaration", "void");

    if(currTok.value.equals("main"))
      consumeToken();
    else
      error("MainDeclaration", "main");

    if(currTok.value.equals("("))
      consumeToken();
    else
      error("MainDeclaration", "(");

    if(currTok.value.equals(")"))
      consumeToken();
    else
      error("MainDeclaration", ")");

    ParseBlock();
  }

  public void ParseFunctionDefinition() {
    db("FunctionDefinition");
    ParseDeclarationType();
    ParseParameterBlock();
    ParseBlock();
  }

  public void ParseDeclarationType() {
  db("DeclarationType");
    ParseDataType();
    ParseIdentifier();
  }

  public void ParseVariableDeclaration() {
    db("VariableDeclaration");
    if(currTok.value.equals("=")) {
      consumeToken();
      ParseConstant();
    }

    if(currTok.value.equals(";"))
      consumeToken();
    else
      error("VariableDeclaration", "missing ;");
  }

  public void ParseFunctionDeclaration() {
    db("FunctionDeclaration");
   ParseParameterBlock();

    if(currTok.value.equals(";"))
      consumeToken();
    else
      error("FunctionDeclaration", "missing ;");
  }

  public void ParseBlock() {
    db("Block");
    if(currTok.value.equals("{"))
      consumeToken();
    else
      error("Block", "missing {");

    while(isFirst(currTok, "Declaration"))
      ParseDeclaration();

    while(isFirst(currTok, "Statement"))
      ParseStatement();

    while(isFirst(currTok, "FunctionDefinition"))
      ParseFunctionDefinition();

    if(currTok.value.equals("}"))
      consumeToken();
    else
      error("Block", "missing }");
  }

  public void ParseParameterBlock() {
    db("ParameterBlock");
    if(currTok.value.equals("("))
      consumeToken();
    else
      error("ParameterBlock", "missing (", currTok.toString());

    if(isFirst(currTok, "Parameter"))
      ParseParameter();

    while(currTok.value.equals(",")) {
      consumeToken();
      ParseParameter();
    }

    if(currTok.value.equals(")"))
      consumeToken();
    else
      error("ParameterBlock", "missing )");
  }

  public void ParseDataType() {
    db("DataType");
    if(isFirst(currTok, "IntegerType"))
      ParseIntegerType();
    else if(isFirst(currTok, "FloatType"))
      ParseFloatType();
    else
      error("DataType", "");
  }

  public  void ParseConstant() {
    db("Constant");
    if(isFirst(currTok, "IntConstant"))
      ParseIntConstant();
    else if(isFirst(currTok, "FloatConstant"))
      ParseFloatConstant();
    else
      error("Constant", "");
  }

  public  void ParseStatement() {
    db("Statement");
    if(isFirst(currTok, "Assignment"))
      ParseAssignment();
    else if(isFirst(currTok, "WhileLoop"))
      ParseWhileLoop();
    else if(isFirst(currTok, "IfStatement"))
      ParseIfStatement();
    else if(isFirst(currTok, "ReturnStatement"))
      ParseReturnStatement();
    else if(isFirst(currTok, "Expression")) {
      ParseExpression();
      if(currTok.value.equals(";"))
        consumeToken();
      else
        error("Statement-Expression", "missing ;");
    }
    else
      error("Statement", "");
  }

  public  void ParseParameter() {
  db("Parameter");
    ParseDataType();
    ParseIdentifier();
  }

  public  void ParseIntegerType() {
    db("IntegerType");
    if(currTok.value.equals("unsigned"))
      consumeToken();

    if(currTok.value.equals("char"))
      consumeToken();
    else if(currTok.value.equals("short"))
      consumeToken();
    else if(currTok.value.equals("int"))
      consumeToken();
    else if(currTok.value.equals("long"))
      consumeToken();
    else
      error("IntegerType", "", currTok.toString());
  }

  public  void ParseFloatType() {
    db("FloatType");
    if(currTok.value.equals("float"))
      consumeToken();
    else if(currTok.value.equals("double"))
      consumeToken();
    else
      error("FloatType", "");
  }

  public  void ParseAssignment() {
    db("Assignment");
    ParseIdentifier();


    if(currTok.value.equals("="))
      consumeToken();
    else
      error("Assignment", "missing =", currTok.toString());

    //For Reference:
    //  https://piazza.com/class/j4ef7ct7iv04wt?cid=67
    //"Two" character lookahead... kind of.
    //Special case:
    while(isFirst(currTok, "Identifier") && tz.PeekNextToken().value.equals("=")) {
      ParseIdentifier();
      if(currTok.value.equals("="))
        consumeToken();
      else
        error("Assignment", "missing =", currTok.toString());
    }

    ParseExpression();

    if(currTok.value.equals(";"))
      consumeToken();
    else
      error("Assignment", "missing ;");
  }

  public  void ParseWhileLoop() {
    db("WhileLoop");
    if(currTok.value.equals("while"))
      consumeToken();
    else
      error("WhileLoop", "missing while");

    if(currTok.value.equals("("))
      consumeToken();
    else
      error("WhileLoop", "missing (");

    ParseExpression();

    if(currTok.value.equals(")"))
      consumeToken();
    else
      error("WhileLoop", "missing )");

    ParseBlock();
  }

  public  void ParseIfStatement() {
    db("IfStatement");
    if(currTok.value.equals("if"))
      consumeToken();
    else
      error("IfStatement", "missing if");

    if(currTok.value.equals("("))
      consumeToken();
    else
      error("IfStatement", "missing (");

    ParseExpression();

    if(currTok.value.equals(")"))
      consumeToken();
    else
      error("IfStatement", "missing )");

    ParseBlock();
  }

  public  void ParseReturnStatement() {
    db("Returnstatement");
    if(currTok.value.equals("return"))
      consumeToken();
    else
      error("ReturnStatement", "missing return");

    ParseExpression();

    if(currTok.value.equals(";"))
      consumeToken();
    else
      error("ReturnStatement", "missing ;");
  }

  public  void ParseExpression() {
    db("Expression");
    ParseSimpleExpression();

    if(isFirst(currTok, "RelationOperator")) {
      ParseRelationOperator();
      if(isFirst(currTok, "SimpleExpression"))
        ParseSimpleExpression();
      else
        error("Expression", "SimpleExpression");
    }

    db1("Expression");
  }

  public  void ParseSimpleExpression() {
    db("SimpleExpression");
    ParseTerm();

    while(isFirst(currTok, "AddOperator")) {
      ParseAddOperator();
      ParseTerm();
    }
  }

  public  void ParseTerm() {
    db("Term");
    ParseFactor();

    while(isFirst(currTok, "MultOperator")) {
      ParseMultOperator();
      ParseFactor();
    }
  }

  public  void ParseFactor() {
    db("Factor");
    if(currTok.value.equals("(")) {
      consumeToken();

      ParseExpression();

      if(currTok.value.equals(")"))
        consumeToken();
      else
        error("Factor", "missing )");
    }
    else if(isFirst(currTok, "Constant"))
      ParseConstant();
    else if(isFirst(currTok, "Identifier")) {
      ParseIdentifier();

      if(currTok.value.equals("(")) {
        consumeToken();

        if(isFirst(currTok, "Expression")) {
          ParseExpression();

          while(currTok.value.equals(",")) {
            consumeToken();
            ParseExpression();
          }
        }

        if(currTok.value.equals(")"))
          consumeToken();
        else
          error("Factor", "missing )", currTok.toString());
      }
    }
  }

  public  void ParseRelationOperator() {
    db("RelationOperator");
    if(currTok.value.equals("=="))
      consumeToken();
    else if(currTok.value.equals("<"))
      consumeToken();
    else if(currTok.value.equals(">"))
      consumeToken();
    else if(currTok.value.equals("<="))
      consumeToken();
    else if(currTok.value.equals(">="))
      consumeToken();
    else if(currTok.value.equals("!="))
      consumeToken();
    else
      error("RelationOperator", "");
  }

  public  void ParseAddOperator() {
    db("AddOperator");
    if(currTok.value.equals("+"))
      consumeToken();
    else if(currTok.value.equals("-"))
      consumeToken();
    else
      error("AddOperator", "");
  }

  public  void ParseMultOperator() {
    db("MultOperator");
    if(currTok.value.equals("*"))
      consumeToken();
    else if(currTok.value.equals("/"))
      consumeToken();
    else
      error("MultOperator", "");
  }

  public  void ParseIdentifier() {
    db("Identifier");
    if(currTok.type.equals("Identifier")) {
      consumeToken();
    }
    else
      error("Identifier", "");

    db1("Identifier");
  }

  public  void ParseIntConstant() {
    db("IntConstant");
    if(currTok.type.equals("IntConstant"))
      consumeToken();
    else
      error("IntConstant", "");
  }

  public  void ParseFloatConstant() {
    db("FloatConstant");
    if(currTok.type.equals("FloatConstant"))
      consumeToken();
    else
      error("FloatConstant", "");
  }
} // class Parser
