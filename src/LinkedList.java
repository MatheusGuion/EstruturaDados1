/*
  Estrutura de Dados I – Apl2 (2025.2)
  GRUPO: BEATRIZ, EDUARDO E MATHEUS  
*/
public class LinkedList {
    private Node head;

    public Node getHead() { return head; } //retorna o nó da cabeça da lista

    public boolean isEmpty() { return head == null; }//verifica se a lista está vazia 

    public Node find(int line) {//encontra um nó baseado no numero da linha, retorna o nó se contrar null 
        Node c = head;//começa pelo nó cabeça 
        while (c != null) {
            if (c.lineNumber == line) return c;//linha encontrada 
            if (c.lineNumber > line) return null;//se o número da linha atual for maior, a linha desejada não existe 
            c = c.next;
        }
        return null;
    }

    // insere ordenado(se existir, apenas atualiza.) Retorna:
    // 0 = inseriu, 1 = atualizou
    public int insertOrUpdate(int line, String instr, StringBuilder oldOut) {
        if (head == null) {
            head = new Node(line, instr);
            return 0;//inseriu 
        }
        if (line < head.lineNumber) {//inserir antes da cabeça (novo head)
            Node n = new Node(line, instr);
            n.next = head;
            head = n;
            return 0;//inseriu 
        }
        Node prev = null;//nó ant 
        Node cur = head;//nó atual 
        while (cur != null && cur.lineNumber < line) {
            prev = cur; cur = cur.next;
        }//atualiza nó existente
        if (cur != null && cur.lineNumber == line) {
            if (oldOut != null) oldOut.append(cur.instruction);
            cur.instruction = instr;//atualiza instrução
            return 1;
        } else {//insere no meio ou no fim 
            Node n = new Node(line, instr);
            prev.next = n;
            n.next = cur;
            return 0;
        }
    }

    //remove linha única; retorna a instrução removida ou null
    public String removeSingle(int line) {
        if (head == null) return null;//lista vazia 
        if (head.lineNumber == line) {
            String s = head.instruction;
            head = head.next;
            return s;
        }
        Node cur = head;
        while (cur.next != null && cur.next.lineNumber < line) cur = cur.next;
        if (cur.next != null && cur.next.lineNumber == line) { //remove nó no meio/fim
            String s = cur.next.instruction;
            cur.next = cur.next.next;
            return s;
        }
        return null;
    }

    // Remove intervalo [a,b] (assume a<=b). Retorna quantidade removida e imprime cada linha em out
    public int removeRange(int a, int b, StringBuilder outLines) {
        int count = 0;
        while (head != null && head.lineNumber >= a && head.lineNumber <= b) {
            if (outLines != null) outLines.append(head.toString()).append('\n');
            head = head.next;
            count++;
        }
        Node cur = head;
        while (cur != null && cur.next != null) { //percorre o resto da lista 
            if (cur.next.lineNumber >= a && cur.next.lineNumber <= b) {
                if (outLines != null) outLines.append(cur.next.toString()).append('\n');
                cur.next = cur.next.next;
                count++;
            } else {
                cur = cur.next;
            }
        }
        return count; //retorno o total de nós removidos 
    }
//calcula o tamanho da lista (n de nós)
    public int size() {
        int n = 0; Node c = head;
        while (c != null) { n++; c = c.next; }
        return n;
    }

    // preenche arrays paralelos com linhas/inst; retorno = quantidade
    public int toArrays(int[] lines, String[] inst) {
        int i = 0; Node c = head;
        while (c != null) {
            lines[i] = c.lineNumber;
            inst[i] = c.instruction;
            i++; c = c.next;
        }
        return i;
    }

    // imprime 20 por tela
    public void listPaged() {
        Node c = head; int shown = 0;
        while (c != null) {
            System.out.println(c);
            shown++;
            if (shown % 20 == 0) {
                System.out.print("");
                try { System.in.read(); } catch (Exception ignored) {}
            }
            c = c.next;
        }
    }
}