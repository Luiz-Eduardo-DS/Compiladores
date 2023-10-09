package lexical;

import exceptions.LexicalException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import utils.TokenType;

public class Scanner {

  int pos;
  int state;
  int linha = 1;
  int coluna = 0;
  int colunaAnterior = 0;
  int linhaAnterior = 1;
  char currentChar;
  char previusChar;
  char[] source_code;
  Map<String, TokenType> reservedWords = new HashMap<>();

  public Scanner(String filename) {
    ReservedWords();
    try {
      String contentBuffer = new String(Files.readAllBytes(Paths.get(filename)),
          StandardCharsets.UTF_8);
      this.source_code = contentBuffer.toCharArray();
      this.pos = 0;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Token nextToken() {
    this.state = 0;
    String content = "";

    while (true) {
      if (isEOF()) {
        return null;
      }

      this.linhaAnterior = this.linha;
      this.colunaAnterior = this.coluna;
      this.previusChar = this.currentChar;
      this.currentChar = this.nextChar();

      if (isComment(this.currentChar)) {
        while (this.currentChar != '\n') {
          this.currentChar = this.nextChar();
        }
      }
      if (this.currentChar == '\n') {
        this.linha++;
        this.coluna = 0;
      }

      switch (this.state) {
        case 0:
          if (this.isLetter(this.currentChar)) {
            content += this.currentChar;
            this.state = 1;
          } else if (isSpace(this.currentChar)) {
            this.state = 0;
          } else if (isDigit(this.currentChar)) {
            content += this.currentChar;
            this.state = 2;
          } else if (isMathOperator(this.currentChar)) {
            content += this.currentChar;
            return new Token(TokenType.MATH_OP, content);
          }else if (isOperator(this.currentChar)) {
            content += this.currentChar;
            this.state = 5;
          } else if (isLeftParenthesis(this.currentChar)) {
            content += this.currentChar;
            return new Token(TokenType.L_PARENTHESIS, content);
          } else if (isRightParenthesis(this.currentChar)) {
            content += this.currentChar;
            return new Token(TokenType.R_PARENTHESIS, content);
          } else if (this.currentChar == ';') {
            content += this.currentChar;
            return new Token(TokenType.SEMICOLON, content);
          } else if (this.currentChar == ':') {
            content += this.currentChar;
            return new Token(TokenType.COLON, content);
          }  else if (IsFloat(this.currentChar)) {
            content += this.currentChar;
            this.state = 6;
          }	else if (isCadeia(this.currentChar)) {
            content += this.currentChar;
            this.state = 8;
          }
          else {
            throw new LexicalException("Invalid Character! Line " + this.linha + " Column " + this.coluna
                + " = " + content + this.currentChar
                + "");
          }
          break;
        case 1:
          if (this.isLetter(this.currentChar) || this.isDigit(this.currentChar)) {
            content += this.currentChar;
            this.state = 1;
          } else {
            this.back();
            TokenType tokenType = this.reservedWords.getOrDefault(content, TokenType.IDENTIFIER);
            return new Token(tokenType, content);
          }
          break;
        case 2:
          if (isDigit(this.currentChar)) {
            content += this.currentChar;
            this.state = 2;
          }  else if (IsFloat(this.currentChar)) {
            content += this.currentChar;
            this.state = 6;
          } else if (isSpace(this.currentChar) || isOperator(this.currentChar) || isMathOperator(
              this.currentChar) || isLeftParenthesis(
              this.currentChar) || isRightParenthesis(
              this.currentChar)) {
            this.back();
            return new Token(TokenType.NUMBER, content);
          } else {
            throw new LexicalException("Number Malformed! Line " + this.linha + " Column " + this.coluna
                + " = " + content + this.currentChar
                + "");
          }
          break;
        case 5:
          if (this.currentChar == '=') {							// para os casos de == | != | <= | >=
            content += this.currentChar;
            return new Token(TokenType.REL_OP, content);
          } else if(this.previusChar == '=') {						// para os casos de =
            this.back();
            return new Token(TokenType.ASSIGNMENT, content);
          } else if(this.previusChar != '!') {						// para os casos de < | >
            this.back();
            return new Token(TokenType.REL_OP, content);
          }
          else {
            throw new RuntimeException(
                "Invalid Relacional Operator ! Line " + this.linha + " Column " + this.coluna + " = " + content + this.currentChar
                    + "");
          }
        case 6:
          if (isDigit(this.currentChar)) {
            content += this.currentChar;
            this.state = 7;
          } else {
            throw new LexicalException("Float Malformed! Line " + this.linha + " Column " + this.coluna + " = " + content + this.currentChar
                + "");
          }
          break;
        case 7:
          if (isDigit(this.currentChar)) {
            content += this.currentChar;
            this.state = 7;
          } else if (isLetter(this.currentChar) || IsFloat(this.currentChar)) {
            throw new LexicalException("Float Malformed! Line " + this.linha + " Column " + this.coluna + " = " + content + this.currentChar
                + "");
          } else {
            this.back();
            return new Token(TokenType.FLOAT, content);
          }
          break;

        case 8:
          if (isCadeia(this.currentChar)) {
            content += this.currentChar;
            return new Token(TokenType.CADEIA, content);
          } else if (isEOF()) {
            throw new LexicalException("String Malformed! Line " + this.linha + " Column " + this.coluna
                + " = " + content + this.currentChar
                + "");
          } else {
            content += this.currentChar;
            this.state = 8;
          }
      }

    }
  }

  private char nextChar() {
    this.coluna++;
    return this.source_code[this.pos++];
  }

  private void back() {
    this.pos--;
  }

  private boolean isLetter(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_');
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isMathOperator(char c) {
    return c == '+' || c == '-' || c == '*' || c == '/';
  }

  private boolean isEOF() {
    if (this.pos >= this.source_code.length) {
      return true;
    }
    return false;
  }

  private boolean isOperator(char c) {
    return c == '>' || c == '<' || c == '!' || c == '=';
  }

  private boolean isLeftParenthesis(char c) {
    return c == '(';
  }

  private boolean isRightParenthesis(char c) {
    return c == ')';
  }

  private boolean isSpace(char c) {
    return c == ' ' || c == '\n' || c == '\t' || c == '\r';
  }

  private boolean IsFloat(char c) {
    return c == '.';
  }

  private void ReservedWords() {
    this.reservedWords.put("ALGORITMO", TokenType.ALGORITMO);
    this.reservedWords.put("DECLARACOES", TokenType.DECLARACOES);
    this.reservedWords.put("INTEIRO", TokenType.INTEIRO);
    this.reservedWords.put("REAL", TokenType.REAL);
    this.reservedWords.put("INPUT", TokenType.LER);
    this.reservedWords.put("IF", TokenType.SE);
    this.reservedWords.put("THEN", TokenType.ENTAO);
    this.reservedWords.put("ELSE", TokenType.ELSE);
    this.reservedWords.put("ASSIGN", TokenType.ASSIGN);
    this.reservedWords.put("TO", TokenType.TO);
    this.reservedWords.put("AND", TokenType.AND);
    this.reservedWords.put("OR", TokenType.OR);
    this.reservedWords.put("PRINT", TokenType.PRINT);
    this.reservedWords.put("WHILE", TokenType.IMPRIMIR);
    this.reservedWords.put("END_ALGORITMO", TokenType.END_ALGORITMO);
  }

  private boolean isComment(char c) {
    return c == '#';
  }

  private boolean isCadeia(char c) {
    return c == '"';
  }
}