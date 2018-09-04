// PART TWO
import java.util.*;
import java.io.*;

//package sax;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class XMLSaxParser extends DefaultHandler
{
  /*
      Elements:
          - XHTMLFORMAT
          - FORMAT
               Subelements:
                   COLOR, STYLE

       Attributes:
          - Background, Foreground, Font, Indent
          - Token

      Links used for logic on XML SAX parsing and the usable functions
          https://www.mkyong.com/java/how-to-read-xml-file-in-java-sax-parser/
          https://docs.oracle.com/javase/tutorial/jaxp/sax/parsing.html
          http://javarevisited.blogspot.com/2011/12/parse-read-xml-file-java-sax-parser.html
          http://sax.sourceforge.net/quickstart.html
    */

 // GLOBAL VARIABLES TO ASSIST WITH FUNCTIONS
  public static boolean given_Indent = false;
  public static boolean post_XHTMLFORMAT = false;
  public static String att_type, att_value;
  public static String att_value_text;
  public static String text;
  public static String tag_name;

  // GLOBALS FOR PRINTING REASONS
  public static String background, foreground, font; // XHTMLFORMAT elements
  public static int indent;
  public static String intConstant, floatConstant, keyword, operator, variable, function;

  // STRINGS TO PRINT
  public static String ic_color, fc_color, kw_color, op_color, var_color, ft_color;
  public static String ic_style, fc_style, kw_style, op_style, var_style, ft_style;

  // VALUES OF EACH TYPE
  public static String ic_c, fc_c, kw_c, op_c, var_c, ft_c;
  public static String ic_s, fc_s, kw_s, op_s, var_s, ft_s;

  public static boolean partTwo;

  public XMLSaxParser() {
    //constructor
    partTwo = false;
  }

  public static void runPartTwo(String fileName, XMLSaxParser handler)
        throws FileNotFoundException, SAXException, IOException, NumberFormatException
  {
    try {
        FileReader file = new FileReader(fileName);

        XMLReader xml_reader = XMLReaderFactory.createXMLReader();
        xml_reader.setContentHandler(handler);
        xml_reader.setErrorHandler(handler);

        xml_reader.parse(new InputSource(file));
    }
    catch (NumberFormatException n) { System.out.println("Error: NumberFormatException in XML format"); }
    catch (FileNotFoundException f) { System.out.println("Error: FileNotFoundException"); }
    catch (SAXException s) { System.out.println("Error: SAXException"); }
    catch (IOException i) { System.out.println("Error: IOException"); }
  }
  /*
   * MAIN FUNCTION
   */
  public static void main(String[] args)
      throws FileNotFoundException, SAXException, IOException, NumberFormatException
  {
    XMLSaxParser handler = new XMLSaxParser();
    String fileName = args[0]; // gets fileName from "first" args

    partTwo = true; // so it deals with printing within testing this file
    runPartTwo(fileName, handler);

  } //end main

  /*
   * Do this stuff when parser first gets to the start element <blah>
   *
   * This function will first read the name within < ___ >
   * and then determine its attribute's information: att_type and att_value
   *
   * Got function delcaration, logic, and parameter names from various resources:
   *      https://docs.oracle.com/javase/tutorial/jaxp/sax/parsing.html
   *      http://sax.sourceforge.net/apidoc/org/xml/sax/ContentHandler.html#startDocument()
   *      http://sax.sourceforge.net/quickstart.html
   */
  public void startElement(String uri, String name, String qName, Attributes atts)
  {
    if ( !(name.equals("XHTMLFORMAT")) && post_XHTMLFORMAT == false && (partTwo == true)) //once no longer that tag
    {
      if (given_Indent == false) {
        indent = 4;
        System.out.println("Indent = " + indent );
      }
      else if (given_Indent == true) {
        System.out.println("Indent = " + indent );
      }

      System.out.println("Background = " + background);
      System.out.println("Foreground = " + foreground);
      System.out.println("Font = " + font);

      post_XHTMLFORMAT = true;
    } // end if post-xhtml = false

    // Dealing with attributes
    int len = atts.getLength();
    for(int i = 0; i < len; i++)
    {
         att_type = atts.getLocalName(i);
         att_value = att_type; // att_type w/o affecting case

         if (att_type.equalsIgnoreCase("Background")) {
           background = atts.getValue(att_value);
         }
         else if (att_type.equalsIgnoreCase("Foreground")) {
           foreground = atts.getValue(att_value);
         }
         else if (att_type.equalsIgnoreCase("Font")) {
           font = atts.getValue(att_value);
         }
         else if (att_type.equalsIgnoreCase("Indent")) {
           String indent_string = atts.getValue(att_value); // get string
           Integer indent_toNum = Integer.valueOf(indent_string); // convert string to num
           indent = indent_toNum; // set indent to num

           given_Indent = true;
         }
         else if (att_type.equalsIgnoreCase("Token")) {
           att_type = "Token";
         }

         att_value_text = atts.getValue(att_value);
       } //end for

  } //end startElement()

  /*
   * Reads the characters between <> and </>
   *
   * Got function parameter variable names from
   * http://www.saxproject.org/apidoc/org/xml/sax/ContentHandler.html
   */
  public void characters(char ch[], int start, int length)
  {
    String content = new String(ch, start, length).trim();
    if (content.matches("[a-zA-Z]+")) {
       text = content;
    }
  } //end characters()

 /*
  * Once parser gets to end element </blah>, it starts to do stuff
  *
  * Currently, we use this to create the strings to print
  * using the globals attribute_type, attribute_value, and text
  *
  * Got function parameter variable names from
  * http://www.saxproject.org/apidoc/org/xml/sax/ContentHandler.html
  */
  public void endElement(String uri, String name, String qName)
  {
    if (qName.equalsIgnoreCase("Style"))
    {
      if (att_value_text.equalsIgnoreCase("IntConstant")) {
        ic_s = text;
        ic_style = "IntConstant Style = " + text;
      }
      else if (att_value_text.equalsIgnoreCase("FloatConstant")) {
        fc_s = text;
        fc_style = "FloatConstant Style = " + text;
      }
      else if (att_value_text.equalsIgnoreCase("Keyword")) {
        kw_s = text;
        kw_style = "Keyword Style = " + text;
      }
      else if (att_value_text.equalsIgnoreCase("Operator")) {
        op_s = text;
        op_style = "Operator Style = " + text;
      }
      else if (att_value_text.equalsIgnoreCase("Variable")) {
        var_s = text;
        var_style = "Variable Style = " + text;
      }
      else if (att_value_text.equalsIgnoreCase("Function")) {
        ft_s = text;
        ft_style = "Function Style = " + text;
      }
    } //end if "style"
    else if( qName.equalsIgnoreCase("Color") )
    {
      if (att_value_text.equalsIgnoreCase("IntConstant")) {
        ic_c = text;
        ic_color = "IntConstant Color = " + text;
      }
      else if (att_value_text.equalsIgnoreCase("FloatConstant")) {
        fc_c = text;
        fc_color = "FloatConstant Color = " + text;
      }
      else if (att_value_text.equalsIgnoreCase("Keyword")) {
        kw_c = text;
        kw_color = "Keyword Color = " + text;
      }
      else if (att_value_text.equalsIgnoreCase("Operator")) {
        op_c = text;
        op_color = "Operator Color = " + text;
      }
      else if (att_value_text.equalsIgnoreCase("Variable")) {
        var_c = text;
        var_color = "Variable Color = " + text;
      }
      else if (att_value_text.equalsIgnoreCase("Function")) {
        ft_c = text;
        ft_color = "Function Color = " + text;
      }
    } //end elif "color"
  } // end endElement()

  /*
   * At end of document, print out the global strings of format info
   * in the given order of example program
   */
  public void endDocument()
  {
    if (partTwo == true)
    {
      String to_print[] = {ic_style, ic_color, fc_style, fc_color, kw_style, kw_color,
                           op_style, op_color, var_style, var_color, ft_style, ft_color };

      for (int i = 0; i < to_print.length; i++)
      {
        if (to_print[i] != null) { //print out the ones that are not empty aka null
          System.out.println(to_print[i]);
        }
      }
    }
  } // end endDocument()

} //end class Part Two
