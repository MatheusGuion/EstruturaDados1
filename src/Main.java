/*
  Estrutura de Dados I – 3ª etapa – 2025.2
  Apl2 – Interpretador de linguagem Assembly simplificada
  Integrantes:
    - <SEU NOME COMPLETO – RA>
  Referências:
    - Enunciado oficial (pág. 2–12) – comandos REPL, regras da linguagem e critérios.
    - Caso tenha usado ChatGPT/similares: cole aqui o(s) link(s) de compartilhamento.

  Observações de implementação:
  - REPL com comandos: LOAD, LIST, RUN, INS, DEL, SAVE, EXIT (case-insensitive).
  - Lista encadeada própria (Node/LinkedList), sem coleções prontas.
  - Registradores A–Z; validação de leitura apenas após MOV no registrador.
  - LIST paginado de 20 em 20 linhas.
  - Mensagens e fluxos espelhados dos exemplos do enunciado.
*/

import java.io.*;
import java.util.Locale;
import java.util.Scanner;

public class Main {

    private static final LinkedList program = new LinkedList();
    private static final int REG_COUNT = 26;
    private static final int[] regs = new int[REG_COUNT];
    private static final boolean[] initialized = new boolean[REG_COUNT];
    private static String currentFile = null;
    private static boolean modified = false;

    public static void main(String[] args) {
        Locale.setDefault(Locale.ROOT);
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            if (!sc.hasNextLine()) break;
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            // Comandos case-insensitive
            String[] toks = line.split("\\s+", 3);
            String cmd = toks[0].toLowerCase(Locale.ROOT);

            try {
                switch (cmd) {
                    case "load" -> {
                        if (toks.length < 2) {
                            System.out.println("Erro: comando inválido.");
                            break;
                        }
                        String path = line.substring(line.indexOf(' ') + 1).trim();
                        handleLoad(sc, path);
                    }
                    case "list" -> {
                        program.listPaged();
                    }
                    case "run" -> {
                        handleRun();
                    }
                    case "ins" -> {
                        if (toks.length < 3) { System.out.println("Erro: comando inválido."); break; }
                        handleIns(toks[1], toks[2]);
                    }
                    case "del" -> {
                        if (toks.length == 2) handleDelSingle(toks[1]);
                        else if (toks.length >= 3) handleDelRange(toks[1], toks[2]);
                        else System.out.println("Erro: comando inválido.");
                    }
                    case "save" -> {
                        if (toks.length == 1) handleSave(sc, currentFile, false);
                        else {
                            String path = line.substring(line.indexOf(' ') + 1).trim();
                            handleSave(sc, path, true);
                        }
                    }
                    case "exit" -> {
                        if (modified) {
                            System.out.println("Arquivo atual (" + quote(currentFile) + ") contém alterações não salvas.");
                            System.out.println("Deseja salvar? (S/N)");
                            System.out.print("> ");
                            String ans = sc.nextLine().trim().toUpperCase(Locale.ROOT);
                            if (ans.startsWith("S")) {
                                if (currentFile == null) {
                                    System.out.println("Erro: nenhum arquivo atual.");
                                } else {
                                    handleSave(sc, currentFile, false);
                                }
                            }
                        }
                        System.out.println("Fim.");
                        return;
                    }
                    default -> System.out.println("Erro: comando inválido.");
                }
            } catch (Exception e) {
                // Evita travar o REPL em caso de exceções não previstas
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    // ======= Comandos =======

    private static void handleLoad(Scanner sc, String path) {
        // Se há arquivo aberto e modificado, perguntar se salva
        if (modified && currentFile != null) {
            System.out.println("Arquivo atual (" + quote(currentFile) + ") contém alterações não salvas.");
            System.out.println("Deseja salvar? (S/N)");
            System.out.print("> ");
            String ans = sc.nextLine().trim().toUpperCase(Locale.ROOT);
            if (ans.startsWith("S")) handleSave(sc, currentFile, false);
        }

        // Tenta abrir o novo arquivo
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            // Limpa tudo
            while (program.getHead() != null) program.removeSingle(program.getHead().lineNumber);
            currentFile = path;
            modified = false;

            String ln;
            while ((ln = br.readLine()) != null) {
                ln = ln.trim();
                if (ln.isEmpty()) continue;
                int space = ln.indexOf(' ');
                if (space < 0) continue; // ignora linhas malformadas
                int num = Integer.parseInt(ln.substring(0, space).trim());
                String instr = ln.substring(space + 1).trim();
                program.insertOrUpdate(num, instr, null);
            }
            System.out.println("Arquivo " + quote(path) + " carregado com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao abrir o arquivo " + quote(path) + ".");
        }
    }

    private static void handleIns(String sLine, String instr) {
        int n;
        try { n = Integer.parseInt(sLine); }
        catch (NumberFormatException ex) { System.out.println("Erro: comando inválido."); return; }
        if (n < 0) { System.out.println("Erro: linha " + n + " inválida."); return; }

        StringBuilder old = new StringBuilder();
        int res = program.insertOrUpdate(n, instr, old);
        modified = true;

        if (res == 0) {
            System.out.println("Linha inserida:");
            System.out.println(formatLine(n, instr));
        } else {
            System.out.println("Linha:");
            System.out.println(formatLine(n, old.toString()));
            System.out.println("Atualizada para:");
            System.out.println(formatLine(n, instr));
        }
    }

    private static void handleDelSingle(String sLine) {
        int n;
        try { n = Integer.parseInt(sLine); }
        catch (NumberFormatException ex) { System.out.println("Erro: comando inválido."); return; }

        String removed = program.removeSingle(n);
        if (removed == null) {
            System.out.println("Erro: linha " + n + " inexistente.");
        } else {
            modified = true;
            System.out.println("Linha removida:");
            System.out.println(formatLine(n, removed));
        }
    }

    private static void handleDelRange(String sA, String sB) {
        int a, b;
        try { a = Integer.parseInt(sA); b = Integer.parseInt(sB); }
        catch (NumberFormatException ex) { System.out.println("Erro: comando inválido."); return; }

        if (b < a) { System.out.println("Erro: intervalo inválido de linhas."); return; }

        StringBuilder out = new StringBuilder();
        int count = program.removeRange(a, b, out);
        if (count == 0) return; // nada a remover -> sem mensagem extra (segue exemplos)
        modified = true;
        System.out.println("Linhas removidas:");
        System.out.print(out.toString());
    }

    private static void handleSave(Scanner sc, String path, boolean mayAskOverwrite) {
        if (path == null) { System.out.println("Erro: nenhum arquivo atual."); return; }

        File f = new File(path);
        if (mayAskOverwrite && f.exists()) {
            System.out.println("Deseja sobrescrever? (S/N)");
            System.out.print("> ");
            String ans = sc.nextLine().trim().toUpperCase(Locale.ROOT);
            if (!ans.startsWith("S")) {
                System.out.println("Arquivo não salvo.");
                return;
            }
        }

        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"))) {
            Node c = program.getHead();
            while (c != null) {
                pw.println(c);
                c = c.next;
            }
            modified = false;
            currentFile = path;
            System.out.println("Arquivo " + quote(path) + " salvo com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao salvar o arquivo " + quote(path) + ".");
        }
    }

    // ======= Interpretador =======

    private static void handleRun() {
        if (program.isEmpty()) {
            System.out.println("Erro: não há código na memória.");
            return;
        }
        // limpa regs
        for (int i = 0; i < REG_COUNT; i++) { regs[i] = 0; initialized[i] = false; }

        // “Carregar” programa em arrays (sem coleções)
        int n = program.size();
        int[] lines = new int[n];
        String[] inst = new String[n];
        program.toArrays(lines, inst);

        int pc = 0; // índice no array
        while (pc >= 0 && pc < n) {
            String current = inst[pc];
            int currentLine = lines[pc];
            try {
                Integer jumpTo = exec(current);
                if (jumpTo != null) {
                    int idx = findLineIndex(lines, jumpTo);
                    if (idx == -1) {
                        System.out.println("Erro: linha " + jumpTo + " inexistente.");
                        System.out.println("Linha: " + currentLine + " " + current);
                        return;
                    }
                    pc = idx;
                } else {
                    pc++;
                }
            } catch (InterpretError e) {
                System.out.println(e.getMessage());
                System.out.println("Linha: " + currentLine + " " + current);
                return;
            }
        }
    }

    // Executa 1 instrução; retorna número de linha para saltar (jnz) ou null
    private static Integer exec(String raw) throws InterpretError {
        String[] p = raw.trim().split("\\s+");
        if (p.length == 0) return null;
        String op = p[0].toLowerCase(Locale.ROOT);

        switch (op) {
            case "mov" -> {
                checkArgs(op, p, 3);
                int x = regIndex(p[1], true); // destino
                int y = value(p[2]);          // pode ser número ou registrador (se já inicializado)
                regs[x] = y;
                initialized[x] = true;
                return null;
            }
            case "inc" -> {
                checkArgs(op, p, 2);
                int x = regIndex(p[1], false);
                ensureInitialized(x, 'X');
                regs[x]++;
                return null;
            }
            case "dec" -> {
                checkArgs(op, p, 2);
                int x = regIndex(p[1], false);
                ensureInitialized(x, 'X');
                regs[x]--;
                return null;
            }
            case "add" -> {
                checkArgs(op, p, 3);
                int x = regIndex(p[1], false);
                ensureInitialized(x, 'X');
                int y = value(p[2]);
                regs[x] += y;
                return null;
            }
            case "sub" -> {
                checkArgs(op, p, 3);
                int x = regIndex(p[1], false);
                ensureInitialized(x, 'X');
                int y = value(p[2]);
                regs[x] -= y;
                return null;
            }
            case "mul" -> {
                checkArgs(op, p, 3);
                int x = regIndex(p[1], false);
                ensureInitialized(x, 'X');
                int y = value(p[2]);
                regs[x] *= y;
                return null;
            }
            case "div" -> {
                checkArgs(op, p, 3);
                int x = regIndex(p[1], false);
                ensureInitialized(x, 'X');
                int y = value(p[2]);
                if (y == 0) throw new InterpretError("Erro: divisão por zero.");
                regs[x] /= y;
                return null;
            }
            case "jnz" -> {
                checkArgs(op, p, 3);
                int x = regIndex(p[1], false);
                ensureInitialized(x, 'X');
                int target;
                try { target = Integer.parseInt(p[2]); }
                catch (NumberFormatException e) { throw new InterpretError("Erro: linha de salto inválida."); }
                if (regs[x] != 0) return target;
                return null;
            }
            case "out" -> {
                checkArgs(op, p, 2);
                int x = regIndex(p[1], false);
                ensureInitialized(x, 'X');
                System.out.println(regs[x]);
                return null;
            }
            default -> throw new InterpretError("Erro: instrução inválida.");
        }
    }

    // ======= Helpers de validação =======

    private static void checkArgs(String op, String[] p, int expected) throws InterpretError {
        if (p.length != expected)
            throw new InterpretError("Erro: instrução " + op + " com quantidade inválida de argumentos.");
    }

    private static int regIndex(String token, boolean dest) throws InterpretError {
        if (token.length() != 1) throw new InterpretError("Erro: registrador " + token.toUpperCase(Locale.ROOT) + " inválido.");
        char ch = Character.toLowerCase(token.charAt(0));
        if (ch < 'a' || ch > 'z') throw new InterpretError("Erro: registrador " + token.toUpperCase(Locale.ROOT) + " inválido.");
        return ch - 'a';
    }

    private static void ensureInitialized(int idx, char label) throws InterpretError {
        if (!initialized[idx]) {
            char regName = (char) ('A' + idx);
            throw new InterpretError("Erro: registrador " + regName + " inválido.");
        }
    }

    private static int value(String tok) throws InterpretError {
        // inteiro ou registrador já inicializado
        try {
            return Integer.parseInt(tok);
        } catch (NumberFormatException ignore) {
            int r = regIndex(tok, false);
            if (!initialized[r]) {
                char name = (char)('A' + r);
                throw new InterpretError("Erro: registrador " + name + " inválido.");
            }
            return regs[r];
        }
    }

    private static int findLineIndex(int[] lines, int target) {
        for (int i = 0; i < lines.length; i++) if (lines[i] == target) return i;
        return -1;
    }

    private static String quote(String s) { return "'" + s + "'"; }

    private static String formatLine(int n, String instr) { return n + " " + instr; }

    // Exceção controlada para mensagens iguais às do PDF
    private static class InterpretError extends Exception {
        public InterpretError(String msg) { super(msg); }
    }
}
