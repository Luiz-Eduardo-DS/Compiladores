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
      String contentBuffer =
          new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
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
          if (isLetter(this.currentChar)) {
            return processIdentifier();
          } else if (isDigit(this.currentChar)) {
            return processNumber();
          } else if (isMathOperator(this.currentChar)) {
            return processMathOperator();
          } else if (isOperator(this.currentChar)) {
            return processRelationalOperator();
          } else if (this.currentChar == '=') {
            return processAssignment();
          } else if (this.currentChar == ';') {
            return processDelimiter();
          } else if (this.currentChar == '<' || this.currentChar == '>') {
            return processRelationalOperator();
          } else if (isLeftParenthesis(this.currentChar)) {
            content = Character.toString(this.currentChar);
            return new Token(
                TokenType.ABRE_PARENTESES, content, this.linhaAnterior, this.colunaAnterior);
          } else if (isRightParenthesis(this.currentChar)) {
            content = Character.toString(this.currentChar);
            return new Token(
                TokenType.FECHA_PARENTESES, content, this.linhaAnterior, this.colunaAnterior);
          } else if (this.currentChar == ':') {
            content = Character.toString(this.currentChar);
            return new Token(
                TokenType.DOISPONTOS, content, this.linhaAnterior, this.colunaAnterior);
          } else if (isCadeia(this.currentChar)) {
            return processCadeia();
          } else {
            throw new LexicalException(
                "Invalid Character! Line " + this.linha + " Column " + this.coluna);
          }
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
          } else if (IsFloat(this.currentChar)) {
            content += this.currentChar;
            this.state = 6;
          } else if (isSpace(this.currentChar)
              || isOperator(this.currentChar)
              || isMathOperator(this.currentChar)
              || isLeftParenthesis(this.currentChar)
              || isRightParenthesis(this.currentChar)) {
            this.back();
            return new Token(TokenType.NUMBER, content);
          } else {
            throw new LexicalException(
                "Number Malformed! Line "
                    + this.linha
                    + " Column "
                    + this.coluna
                    + " = "
                    + content
                    + this.currentChar
                    + "");
          }
          break;
        case 5:
          if (this.currentChar == '=') {
            content += this.currentChar;
            return new Token(TokenType.REL_OP, content);
          } else if (this.previusChar == '=') {
            this.back();
            return new Token(TokenType.ASSIGNMENT, content);
          } else if (this.previusChar != '!') {
            this.back();
            return new Token(TokenType.REL_OP, content);
          } else {
            throw new RuntimeException(
                "Invalid Relacional Operator ! Line "
                    + this.linha
                    + " Column "
                    + this.coluna
                    + " = "
                    + content
                    + this.currentChar
                    + "");
          }
        case 6:
          if (isDigit(this.currentChar)) {
            content += this.currentChar;
            this.state = 7;
          } else {
            throw new LexicalException(
                "Float Malformed! Line "
                    + this.linha
                    + " Column "
                    + this.coluna
                    + " = "
                    + content
                    + this.currentChar
                    + "");
          }
          break;
        case 7:
          if (isDigit(this.currentChar)) {
            content += this.currentChar;
            this.state = 7;
          } else if (isLetter(this.currentChar) || IsFloat(this.currentChar)) {
            throw new LexicalException(
                "Float Malformed! Line "
                    + this.linha
                    + " Column "
                    + this.coluna
                    + " = "
                    + content
                    + this.currentChar
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
            throw new LexicalException(
                "String Malformed! Line "
                    + this.linha
                    + " Column "
                    + this.coluna
                    + " = "
                    + content
                    + this.currentChar
                    + "");
          } else {
            content += this.currentChar;
            this.state = 8;
          }
      }
    }
  }

  private Token processIdentifier() {
    StringBuilder content = new StringBuilder();
    while (isLetter(this.currentChar) || isDigit(this.currentChar)) {
      content.append(this.currentChar);
      this.currentChar = this.nextChar();
    }
    TokenType tokenType = TokenType.IDENTIFIER;
    if (reservedWords.containsKey(content.toString())) {
      tokenType = reservedWords.get(content.toString());
    }
    return new Token(tokenType, content.toString(), this.linhaAnterior, this.colunaAnterior);
  }

  private Token processNumber() {
    StringBuilder content = new StringBuilder();
    while (isDigit(this.currentChar)) {
      content.append(this.currentChar);
      this.currentChar = this.nextChar();
    }
    return new Token(TokenType.NUMBER, content.toString(), this.linhaAnterior, this.colunaAnterior);
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
    this.reservedWords.put("ELSE", TokenType.SENAO);
    this.reservedWords.put("ASSIGN", TokenType.ASSIGN);
    this.reservedWords.put("TO", TokenType.TO);
    this.reservedWords.put("AND", TokenType.AND);
    this.reservedWords.put("OR", TokenType.OR);
    this.reservedWords.put("WHILE", TokenType.IMPRIMIR);
    this.reservedWords.put("FIM", TokenType.FIM);
  }

  private boolean isComment(char c) {
    return c == '#';
  }

  private boolean isCadeia(char c) {
    return c == '"';
  }

  private Token processCadeia() {
    StringBuilder content = new StringBuilder();
    content.append('"');
    while (this.currentChar != '"') {
      content.append(this.currentChar);
      this.currentChar = this.nextChar();
      if (isEOF()) {
        throw new LexicalException(
            "String Malformed! Line "
                + this.linhaAnterior
                + " Column "
                + this.colunaAnterior
                + " = "
                + content.toString());
      }
    }
    content.append('"');
    this.currentChar = this.nextChar();
    return new Token(TokenType.CADEIA, content.toString(), this.linhaAnterior, this.colunaAnterior);
  }

  private Token processRelationalOperator() {
    StringBuilder content = new StringBuilder();
    content.append(this.currentChar);
    this.currentChar = this.nextChar();

    if (this.currentChar == '=') {
      content.append(this.currentChar);
      this.currentChar = this.nextChar();
    }

    return new Token(TokenType.REL_OP, content.toString(), this.linhaAnterior, this.colunaAnterior);
  }

  private Token processAssignment() {
    String content = Character.toString(this.currentChar);
    this.currentChar = this.nextChar();
    return new Token(TokenType.ASSIGNMENT, content, this.linhaAnterior, this.colunaAnterior);
  }

  private Token processMathOperator() {
    String content = Character.toString(this.currentChar);
    this.currentChar = this.nextChar();
    return new Token(TokenType.MATH_OP, content, this.linhaAnterior, this.colunaAnterior);
  }

  private Token processDelimiter() {
    String content = Character.toString(this.currentChar);
    this.currentChar = this.nextChar();
    return new Token(TokenType.DELIM, content, this.linhaAnterior, this.colunaAnterior);
  }
}
