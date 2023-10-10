package syntax;

import exceptions.ParserException;
import lexical.Scanner;
import lexical.Token;
import utils.TokenType;

public class Parser {

  private Scanner scanner;
  private Token token;

  public Parser(Scanner scanner) {
    this.scanner = scanner;
    this.token = this.scanner.nextToken();
  }

  public void E() throws Exception {
    T();
    El();
  }

  public void El() throws Exception {
    if (this.token.getTipo() == TokenType.MATH_OP) {
      OP();
      T();
      El();
    }
  }

  public void T() throws Exception {
    if (this.token.getTipo() != TokenType.IDENTIFIER && this.token.getTipo() != TokenType.NUMBER) {
      throw new ParserException(
          "Expected IDENTIFIER or NUMBER, found "
              + this.token.getTipo()
              + ": "
              + this.token.getValor()
              + " na linha: "
              + this.token.getLinha()
              + " coluna: "
              + this.token.getColuna());
    }
    this.token = this.scanner.nextToken();
  }

  public void OP() throws Exception {
    if (this.token.getTipo() != TokenType.MATH_OP) {
      throw new ParserException(
          "Expected MATH_OPERATOR, found " + this.token.getTipo() + ": " + this.token.getValor());
    }
    this.token = this.scanner.nextToken();
  }

  public void parsePrograma() throws Exception {
    match(TokenType.DOISPONTOS);
    match(TokenType.DECLARACOES);
    listaDeclaracoes();
    match(TokenType.DOISPONTOS);
    match(TokenType.ALGORITMO);
    listaComandos();
  }

  private void listaDeclaracoes() throws Exception {
    declaracao();
    while (this.token.getTipo() == TokenType.VARIAVEL) {
      declaracao();
    }
  }

  private void declaracao() throws Exception {
    match(TokenType.VARIAVEL);
    match(TokenType.DOISPONTOS);
    tipoVar();
    match(TokenType.DELIM);
  }

  private void tipoVar() throws Exception {
    if (this.token.getTipo() == TokenType.INTEIRO || this.token.getTipo() == TokenType.REAL) {
      this.token = this.scanner.nextToken();
    } else {
      throw new Exception("Erro de sintaxe: tipoVar esperado");
    }
  }

  private void listaComandos() throws Exception {
    comando();
    while (this.token.getTipo() != TokenType.FIM) {
      comando();
    }
  }

  private void comando() throws Exception {
    if (this.token.getTipo() == TokenType.VARIAVEL) {
      comandoAtribuicao();
    } else if (this.token.getTipo() == TokenType.LER) {
      comandoEntrada();
    } else if (this.token.getTipo() == TokenType.IMPRIMIR) {
      comandoSaida();
    } else if (this.token.getTipo() == TokenType.SE) {
      comandoCondicao();
    } else if (this.token.getTipo() == TokenType.ENQUANTO) {
      comandoRepeticao();
    } else if (this.token.getTipo() == TokenType.INICIO) {
      subAlgoritmo();
    } else {
      throw new Exception("Erro de sintaxe: Comando não reconhecido");
    }
  }

  private void comandoAtribuicao() throws Exception {
    match(TokenType.VARIAVEL);
    match(TokenType.ASSIGNMENT);
    expressaoAritmetica();
    match(TokenType.DELIM);
  }

  private void comandoEntrada() throws Exception {
    match(TokenType.LER);
    match(TokenType.VARIAVEL);
    match(TokenType.DELIM);
  }

  private void comandoSaida() throws Exception {
    match(TokenType.IMPRIMIR);
    match(TokenType.ABRE_PARENTESES);
    if (this.token.getTipo() == TokenType.VARIAVEL || this.token.getTipo() == TokenType.CADEIA) {
      this.token = this.scanner.nextToken();
    } else {
      throw new Exception("Erro de sintaxe: Expressão inválida após IMPRIMIR");
    }
    match(TokenType.FECHA_PARENTESES);
    match(TokenType.DELIM);
  }

  private void comandoCondicao() throws Exception {
    match(TokenType.SE);
    expressaoRelacional();
    match(TokenType.ENTAO);
    comando();
    if (this.token.getTipo() == TokenType.SENAO) {
      match(TokenType.SENAO);
      comando();
    }
  }

  private void comandoRepeticao() throws Exception {
    match(TokenType.ENQUANTO);
    expressaoRelacional();
    comando();
  }

  private void subAlgoritmo() throws Exception {
    match(TokenType.INICIO);
    listaComandos();
    match(TokenType.FIM);
  }

  private void expressaoRelacional() throws Exception {
    termoRelacional();
    while (this.token.getTipo() == TokenType.OP_REL) {
      match(TokenType.OP_REL);
      termoRelacional();
    }
  }

  private void termoRelacional() throws Exception {
    expressaoAritmetica();
    if (this.token.getTipo() == TokenType.OP_REL) {
      match(TokenType.OP_REL);
      expressaoAritmetica();
    }
  }

  private void expressaoAritmetica() throws Exception {
    termoAritmetico();
    while (this.token.getTipo() == TokenType.MATH_OP) {
      match(TokenType.MATH_OP);
      termoAritmetico();
    }
  }

  private void termoAritmetico() throws Exception {
    fatorAritmetico();
    while (this.token.getTipo() == TokenType.MATH_OP) {
      match(TokenType.MATH_OP);
      fatorAritmetico();
    }
  }

  private void fatorAritmetico() throws Exception {
    if (this.token.getTipo() == TokenType.IDENTIFIER || this.token.getTipo() == TokenType.NUMBER) {
      match(this.token.getTipo());
    } else if (this.token.getTipo() == TokenType.ABRE_PARENTESES) {
      match(TokenType.ABRE_PARENTESES);
      expressaoAritmetica();
      match(TokenType.FECHA_PARENTESES);
    } else {
      throw new Exception("Erro de sintaxe: Fator aritmético inválido");
    }
  }

  private void match(TokenType expectedTokenType) throws Exception {
    if (this.token.getTipo() == expectedTokenType) {
      this.token = this.scanner.nextToken();
    } else {
      throw new Exception("Erro de sintaxe: Token esperado não encontrado");
    }
  }
}
