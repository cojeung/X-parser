// PART FOUR
import java.util.*;
import java.io.*;

//package sax;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;


public class Translator
{
  //1 tab = 3 &nbsp, 2 tab = 5 &nbsp and 3 tab = 7 &nbsp

  /*
      LOGIC:
        f = function
        v = variable
        block #
        identifier name

        1 indent = "& nbsp"
        2 indents = "& nbsp" + " "
        3 or more indents = "& nbsp & nbsp" + alternate between " " and "& nbsp"

   */
  // "GLOBALS"
  public static XMLSaxParser xmlsp = new XMLSaxParser();
  public static FileWriter fw;
  public static PrintWriter pw;
  public static EXParser exp;
  public static String x_file;
  public static String xml_file;

  public static int indentCount = 0; //ADD when enter new block, SUBTRACT when exit block

  public static String prevToken = "";

  // public void SymbolTable(){
  //   List<Hashtable> symbolTable = new ArrayList();
  //   List<MyType> myList = new ArrayList<MyType>();
  //
  // }
  //

  public static class EXParser extends Parser {
    public EXParser() {
      super(x_file); // CHANGE THIS
      //debug = true;
    }

    public void indent(PrintWriter pw){
      for (int i = 0; i < indentCount; i++)
      {
        for(int j = 0; j < xmlsp.indent; j++)
        {
          pw.print("&nbsp;");
        }
      }
    }

    public void ParseBlock() {
      //We need to overwrite more of ParseBlock than just calling the super class.
        db("Block");
        if(currTok.value.equals("{")) {
            consumeToken();
            indentCount++;
            pw.print("<br />\n");
        }
        else
            error("Block", "missing {");

        while(isFirst(currTok, "Declaration"))
            ParseDeclaration();

        while(isFirst(currTok, "Statement"))
            ParseStatement();

        while(isFirst(currTok, "FunctionDefinition"))
            ParseFunctionDefinition();

        if(currTok.value.equals("}")) {
            indentCount--;
            indent(pw);
            consumeToken();
            pw.print("<br />\n");
        }
        else
            error("Block", "missing }");

    } //end parseBlock

    public void ParseStatement() {
      indent(pw);
      super.ParseStatement();
      //pw.print("<br />");
    } //end ParseStatement

    public void ParseAssignment() {
      super.ParseAssignment();
      pw.print("<br />\n");
    } // end ParseAssignment

    public void ParseReturnStatement() {
      super.ParseReturnStatement();
      pw.print("<br />\n");
    } // end ParseReturnStatement

    public void ParseDeclaration() {
      indent(pw);
      super.ParseDeclaration();
      pw.print("<br />\n");
    } //end ParseDeclaration

    public void ParseFunctionDefinition() {
      //pw.print("<br />");
      indent(pw);
      super.ParseFunctionDefinition();
    } //end ParseFunctionDefinition

    public void consumeToken() {

      // if (prevToken.equals("(") && ( !(currTok.type.equals("IntConstant"))
      //     && !(currTok.type.equals("FloatConstant"))) )
      // {
      //   pw.print(" ");
      // }


      if (! (currTok.value.equals("(") || currTok.value.equals(")") ||
             currTok.value.equals("{") || currTok.value.equals("}") ||
             currTok.value.equals(";") || currTok.value.equals(",")))
      { //this is for printing spaces properly
        if (! ( prevToken.equals("(") || prevToken.equals("{") ||
                prevToken.equals("}") || prevToken.equals(";") ) )
              {
                pw.print(" ");

              }
      } //end double if

      if (prevToken.equals(",")) { pw.print(" ");}

      if (currTok.type.equals("Keyword")){
        //indent(pw);
        pw.print("<font color=\"" + xmlsp.kw_c + "\">");
        pw.print("<" + xmlsp.kw_s + ">" + currTok.value + "</" + xmlsp.kw_s + ">");
        pw.print("</font>");
      } //if
      else if (currTok.type.equals("IntConstant")){
        pw.print("<font color=\"" + xmlsp.ic_c + "\">");
        pw.print("<" + xmlsp.ic_s + ">" + currTok.value + "</" + xmlsp.ic_s + ">");
        pw.print("</font>");
      } //if
      else if (currTok.type.equals("FloatConstant")){
        pw.print("<font color=\"" + xmlsp.fc_c + "\">");
        pw.print("  <" + xmlsp.fc_s + ">" + currTok.value + "</" + xmlsp.fc_s + ">");
        pw.print("</font>");
      } //if
      else if (currTok.type.equals("Operator")){
        pw.print("<font color=\"" + xmlsp.op_c + "\">");

        pw.print("<" + xmlsp.op_s + ">");

        if (currTok.value.equals("<")) {
          pw.print("&lt; ");
        }
        else if (currTok.value.equals(">")) {
          pw.print("&gt; ");
        }
        else if (currTok.value.equals("<=")) {
          pw.print("&lt;= ");
        }
        else if (currTok.value.equals(">=")) {
          pw.print("&gt;= ");
        }
        else {
          pw.print(currTok.value);
        }

        pw.print("</" + xmlsp.op_s + ">");
        pw.print("</font>");

      } //if
      else { // where we differentiate stuff
        pw.print("<font color=\"" + xmlsp.var_c + "\">");

        if (xmlsp.var_s != null)
        {
          pw.print("  <" + xmlsp.var_s + ">" + currTok.value + "</" + xmlsp.var_s + ">");
        }
        else {
          //pw.print(currTok.value);
          pw.print("<a>" + currTok.value + "</a>");
        }

        pw.print("</font>");

        /*
          Need to deal wtih links for <a>
              <a name = "f0Foo" where f = fumction, # is block, Foo is ID name
              <a href = "#v2TestFloat" where
        */

      }
      prevToken = currTok.value; //store curr tok as global prevToken
      super.consumeToken();
    } //end consumeToken()
  } // end class EXParser

  public void printTheRest(PrintWriter pw)
  {
    exp = new EXParser();
    exp.ParseProgram();


  } //end printTheRest

  public void ConvertFile(String x_fileName) throws FileNotFoundException, IOException
  {
    String xhtml_fileName = x_fileName.replace(".x", ".xhtml");

    //refered to:
    //http://www.cs.utexas.edu/~mitra/csSummer2012/cs312/lectures/fileIO.html

    File output_xhtmlFile = new File(xhtml_fileName);

    fw = new FileWriter(output_xhtmlFile);
    pw = new PrintWriter(fw);

    // // just writes contents of .x file into .xhtml
    // int i;
    // while( (i = fis.read() ) != -1 )
    // {
    //   char c = (char) i;
    //   pw.print(c);
    // }

    pw.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"> ");
    pw.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">");
    pw.println("<head>");
    pw.println("<title>");
    pw.println("X Formatted file</title>");
    pw.println("</head>");

    pw.print("<body bgcolor=\"" + xmlsp.background + "\""
                + " text=\"" + xmlsp.foreground + "\""
                + " link=\"" + xmlsp.foreground + "\""
                + " vlink=\"" + xmlsp.foreground + "\">\n");

    pw.print("<font face=\"" + xmlsp.font + "\">\n");

    printTheRest(pw);

    pw.println("</font>");
    pw.println("</body>");
    pw.println("</html>");

    pw.close(); //close the file
  }

  public static void main(String[] args)
      throws FileNotFoundException, SAXException, IOException, NumberFormatException, NullPointerException
  {
    String fileName1 = args[0]; //first arg
    String fileName2 = args[1]; //second args

    // DETERMINE WHICH IS .X AND .XML FILE
    String f1_ext, f2_ext;
    x_file = fileName1;
    xml_file = fileName2;

    f1_ext = fileName1.substring(fileName1.length() - 1);
    f2_ext = fileName2.substring(fileName2.length() - 1);

    if (f1_ext.equals("x")) {
      x_file = fileName1;
    } else if (f1_ext.equals("l")) {
      xml_file = fileName1;
    }
    else { System.exit(1); }

    if (f2_ext.equals("x")) {
      x_file = fileName2;
    } else if (f2_ext.equals("l")) {
      xml_file = fileName2;
    }
    else { System.exit(1); }


    Translator translator = new Translator();
    try {
      //fis = new FileInputStream(fileName);
      xmlsp.runPartTwo(xml_file, xmlsp);


      translator.ConvertFile(x_file);
    }
    catch (NumberFormatException n) { System.out.println("Error: NumberFormatException in XML format"); }
    catch (FileNotFoundException f) { System.out.println("Error: FileNotFoundException"); }
    catch (SAXException s) { System.out.println("Error: SAXException"); }
    catch (IOException i) { System.out.println("Error: IOException"); }
    catch (NullPointerException n) { System.out.println("Error: NullPointerException"); }
  }

} //end class Translator
