import exceptions.LexicalException;
import exceptions.SyntaxException;
import lexical.Scanner;
import syntax.Parser;

public class Main {
  public static void main(String[] args) {
    try {
      Scanner scanner =
          new Scanner(
              "C:\\Users\\luizd\\IdeaProjects\\projeto_compiladores\\src\\main\\resources\\programa_checkpoint2.mc");
      Parser parser = new Parser(scanner);
      parser.E();
      System.out.println("Compilation successful!");
    } catch (LexicalException e) {
      System.out.println("Lexical error: " + e.getMessage());
    } catch (SyntaxException e) {
      System.out.println("Syntax error: " + e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
