/*
  Estrutura de Dados I – Apl2 (2025.2)
  Integrantes: BEATRIZ SILVA NÓBREGA - 10435789
               EDUARDO KENJI HERNANDES IKEMATU - 10439924
               MATHEUS GUION - 10437683
*/

public class Node { //classe que representa um nó de uma lista encadeada
    public int lineNumber; //número da linha referente ao comando ou instrução
    public String instruction; //texto da instrução sem o número da linha
    public Node next;  //referência para o próximo nó da lista

    //construtor que inicializa os atributos do nó
    public Node(int lineNumber, String instruction) {
        this.lineNumber = lineNumber;     //recebe o número da linha
        this.instruction = instruction;   //recebe o texto da instrução
        this.next = null;                 //próximo nó começa nulo
    }

    //sobrescreve o método toString para facilitar a impressão do nó
    @Override
    public String toString() {
        return String.format("%d %s", lineNumber, instruction);
    }
}