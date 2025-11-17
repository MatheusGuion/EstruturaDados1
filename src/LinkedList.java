/*
  Estrutura de Dados I – Apl2 (2025.2)
  Lista encadeada simples (sem usar coleções da linguagem)
  Responsável por manter as linhas SEMPRE ordenadas pelo número.
*/
public class LinkedList {
    private Node head;

    public Node getHead() { return head; }

    public boolean isEmpty() { return head == null; }

    public Node find(int line) {
        Node c = head;
        while (c != null) {
            if (c.lineNumber == line) return c;
            if (c.lineNumber > line) return null;
            c = c.next;
        }
        return null;
    }

    // Insere ordenado; se existir, apenas atualiza. Retorna:
    // 0 = inseriu, 1 = atualizou
    public int insertOrUpdate(int line, String instr, StringBuilder oldOut) {
        if (head == null) {
            head = new Node(line, instr);
            return 0;
        }
        if (line < head.lineNumber) {
            Node n = new Node(line, instr);
            n.next = head;
            head = n;
            return 0;
        }
        Node prev = null;
        Node cur = head;
        while (cur != null && cur.lineNumber < line) {
            prev = cur; cur = cur.next;
        }
        if (cur != null && cur.lineNumber == line) {
            if (oldOut != null) oldOut.append(cur.instruction);
            cur.instruction = instr;
            return 1;
        } else {
            Node n = new Node(line, instr);
            prev.next = n;
            n.next = cur;
            return 0;
        }
    }

    // Remove linha única; retorna a instrução removida ou null
    public String removeSingle(int line) {
        if (head == null) return null;
        if (head.lineNumber == line) {
            String s = head.instruction;
            head = head.next;
            return s;
        }
        Node cur = head;
        while (cur.next != null && cur.next.lineNumber < line) cur = cur.next;
        if (cur.next != null && cur.next.lineNumber == line) {
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
        while (cur != null && cur.next != null) {
            if (cur.next.lineNumber >= a && cur.next.lineNumber <= b) {
                if (outLines != null) outLines.append(cur.next.toString()).append('\n');
                cur.next = cur.next.next;
                count++;
            } else {
                cur = cur.next;
            }
        }
        return count;
    }

    public int size() {
        int n = 0; Node c = head;
        while (c != null) { n++; c = c.next; }
        return n;
    }

    // Preenche arrays paralelos com linhas/inst; retorno = quantidade
    public int toArrays(int[] lines, String[] inst) {
        int i = 0; Node c = head;
        while (c != null) {
            lines[i] = c.lineNumber;
            inst[i] = c.instruction;
            i++; c = c.next;
        }
        return i;
    }

    // Imprime 20 por tela
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
