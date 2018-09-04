public class Token {
    public String value;
    public String type;
    public int row_num;
    public int col_num;

    public Token() {
      value = "";
      type = "";
      row_num = -1;
      col_num = -1;
    }

    public Token(String tv) {
      value = tv;
      type = "";
      row_num = -1;
      col_num = -1;
    }

    public Token(String tv, String tt) {
      value = tv;
      type = tt;
      row_num = -1;
      col_num = -1;
    }

    public Token(String tv, String tt, int r, int c) {
      value = tv;
      type = tt;
      row_num = r;
      col_num = c;
    }

    public String toString() {
      return new String(String.format("@%4d,%4d %13s \"%s\"", row_num, col_num, type, value));
    }
  } // class Token
