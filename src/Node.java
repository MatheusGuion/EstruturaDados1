/*
  Estrutura de Dados I – Apl2 (2025.2)
  Integrantes: BEATRIZ SILVA NÓBREGA - 10435789

  Observação: não usamos coleções prontas (ArrayList/LinkedList/Map...), só ponteiros
*/
public class Node {
    public int lineNumber;     // número da linha
    public String instruction; // texto da instrução (sem o número)
    public Node next;

    public Node(int lineNumber, String instruction) {
        this.lineNumber = lineNumber;
        this.instruction = instruction;
        this.next = null;
    }

    @Override
    public String toString() {
        return String.format("%d %s", lineNumber, instruction);
    }
}
