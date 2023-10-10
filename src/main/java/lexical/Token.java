package lexical;

import utils.TokenType;

public class Token {

  private TokenType tipo;
  private String valor;
  private int linha;
  private int coluna;

  public Token(TokenType tipo, String valor, int linha, int coluna) {
    this.tipo = tipo;
    this.valor = valor;
    this.linha = linha;
    this.coluna = coluna;
  }

  public Token(TokenType tipo, String valor) {
    this(tipo, valor, -1, -1);
  }

  public Token(int linha, int coluna) {
    this(null, null, linha, coluna);
  }

  public TokenType getTipo() {
    return tipo;
  }

  public void setTipo(TokenType tipo) {
    this.tipo = tipo;
  }

  public String getValor() {
    return valor;
  }

  public void setValor(String valor) {
    this.valor = valor;
  }

  public int getLinha() {
    return linha;
  }

  public int getColuna() {
    return coluna;
  }

  @Override
  public String toString() {
    return "Token [tipo=" + tipo + ", valor=" + valor + ", linha=" + linha + ", coluna=" + coluna + "]";
  }
}