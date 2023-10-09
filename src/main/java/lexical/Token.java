package lexical;

import utils.TokenType;

public class Token {

  private TokenType tipo;
  private String valor;
  private int linha;
  private int coluna;

  public Token(TokenType tipo, String valor) {
    super();
    this.tipo = tipo;
    this.valor = valor;
  }

  public TokenType getTipo() {
    return this.tipo;
  }

  public void setTipo(TokenType tipo) {
    this.tipo = tipo;
  }

  public String getValor() {
    return this.valor;
  }

  public void setValor(String valor) {
    this.valor = valor;
  }

  public int getLinha() {
    return this.linha;
  }

  public int getColuna() {
    return this.coluna;
  }

  @Override
  public String toString() {
    return "Token [tipo=" + this.tipo + ", valor=" + this.valor + "]";
  }

}