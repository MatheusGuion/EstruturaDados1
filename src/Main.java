/*
  Estrutura de Dados I – 3ª etapa – 2025.2
  Apl2 – Interpretador de linguagem Assembly simplificada
  Integrantes:
    - Matheus Guion - RA: 10437693
    - Beatriz Silva Nóbrega - RA: 10435789
    - Eduardo Kenji - RA: 10439924
*/

import java.io.*;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    
    //implementação da Lista encadeada que recebe as linhas de código
    private static final LinkedList program = new LinkedList();
    
    //declaração dos registradores, seus valores e o se foi feito o uso de MOV neles 
    private static final int REG_COUNT = 26;
    private static final int[] regs = new int[REG_COUNT];
    private static final boolean[] initialized = new boolean[REG_COUNT];

    //controle do arquivo e aviso de arquivo não salvo 
    private static String currentFile = null;
    private static boolean modified = false;

    public static void main(String[] args) {
        Locale.setDefault(Locale.ROOT);
        Scanner sc = new Scanner(System.in);

        //REPL principal 
        while (true) {
            System.out.print("> ");
            if (!sc.hasNextLine()) break;
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            // divide o comando em até 3 partes (comando, argumento e resto)
            String[] toks = line.split("\\s+", 3);
            String cmd = toks[0].toLowerCase(Locale.ROOT);//case insesitive

            try {
                switch (cmd) {
                    case "load" -> {
                        if (toks.length < 2) {
                            System.out.println("Erro: comando inválido.");
                            break;
                        }
                        //pega todo o restante da lista, permitindo textos usando espaço 
                        String path = line.substring(line.indexOf(' ') + 1).trim();
                        handleLoad(sc, path);
                    }
                    case "list" -> {
                        // LIST paginado (implementado na Linked List)
                        program.listPaged();
                    }
                    case "run" -> {
                        //executa o interpretador
                        handleRun();
                    }
                    case "ins" -> {
                        // INS linha instrução 
                        if (toks.length < 3) { System.out.println("Erro: comando inválido."); break; }
                        handleIns(toks[1], toks[2]);
                    }
                    case "del" -> {
                        // DEL n ou DEL a b
                        if (toks.length == 2) handleDelSingle(toks[1]);
                        else if (toks.length >= 3) handleDelRange(toks[1], toks[2]);
                        else System.out.println("Erro: comando inválido.");
                    }
                    case "save" -> {
                        // SAVE [nome do arquivo]
                        if (toks.length == 1) handleSave(sc, currentFile, false);
                        else {
                            String path = line.substring(line.indexOf(' ') + 1).trim();
                            handleSave(sc, path, true);
                        }
                    }
                    case "exit" -> {
                        // se houver alterações, pergunta se deseja salvar
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

    //LOAD: carrega arquivo, perguntando para mudar o atual caso modificado 
    private static void handleLoad(Scanner sc, String path) {
        if (modified && currentFile != null) {
            System.out.println("Arquivo atual (" + quote(currentFile) + ") contém alterações não salvas.");
            System.out.println("Deseja salvar? (S/N)");
            System.out.print("> ");
            String ans = sc.nextLine().trim().toUpperCase(Locale.ROOT);
            if (ans.startsWith("S")) handleSave(sc, currentFile, false);
        }

        // Tenta abrir o novo arquivo
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            // Limpa o programa atual nó a nó
            while (program.getHead() != null) program.removeSingle(program.getHead().lineNumber);
            currentFile = path;
            modified = false;

            String ln;
            while ((ln = br.readLine()) != null) {
                ln = ln.trim();
                if (ln.isEmpty()) continue;
                //formato esperado: "<numero> <instrução>"
                int space = ln.indexOf(' ');
                if (space < 0) continue; // ignora linhas malformadas
                int num = Integer.parseInt(ln.substring(0, space).trim());
                String instr = ln.substring(space + 1).trim();
                //insere/atualiza a instrução na LinkedList
                program.insertOrUpdate(num, instr, null);
            }
            System.out.println("Arquivo " + quote(path) + " carregado com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao abrir o arquivo " + quote(path) + ".");
        }
    }

    //INS: insere ou atualiza linha
    private static void handleIns(String sLine, String instr) {
        int n;
        try { n = Integer.parseInt(sLine); }
        catch (NumberFormatException ex) { System.out.println("Erro: comando inválido."); return; }
        if (n < 0) { System.out.println("Erro: linha " + n + " inválida."); return; }

        StringBuilder old = new StringBuilder();
        int res = program.insertOrUpdate(n, instr, old);// res = 0 (insere), res != 0 (atualizou)
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

    //DEL em apenas uma linha
    private static void handleDelSingle(String sLine) {
        int n;
        try { n = Integer.parseInt(sLine); }
        catch (NumberFormatException ex) { System.out.println("Erro: comando inválido."); return; }

        //Chama a remoção por meio da LinkedList
        String removed = program.removeSingle(n);
        if (removed == null) {
            System.out.println("Erro: linha " + n + " inexistente.");
        } else {
            modified = true;
            System.out.println("Linha removida:");
            System.out.println(formatLine(n, removed));
        }
    }

    //DEL em um intervalos de linhas (a e b)
    private static void handleDelRange(String sA, String sB) {
        int a, b;
        try { a = Integer.parseInt(sA); b = Integer.parseInt(sB); }
        catch (NumberFormatException ex) { System.out.println("Erro: comando inválido."); return; }

        if (b < a) { System.out.println("Erro: intervalo inválido de linhas."); return; }

        StringBuilder out = new StringBuilder();
        int count = program.removeRange(a, b, out);//remove e concatena linhas no out
        if (count == 0) return; // nada a remover -> sem mensagem extra (segue exemplos)
        modified = true;
        System.out.println("Linhas removidas:");
        System.out.print(out.toString());
    }

    //SAVE: salva o programa atual no disco. Pergunta para sobrescrever se o caminho é novo
    private static void handleSave(Scanner sc, String path, boolean mayAskOverwrite) {
        if (path == null) { System.out.println("Erro: nenhum arquivo atual."); return; }

        File f = new File(path);
        //pergunta se deseja sobrescrever um arquivo existente, se necessário
        if (mayAskOverwrite && f.exists()) {
            System.out.println("Deseja sobrescrever? (S/N)");
            System.out.print("> ");
            String ans = sc.nextLine().trim().toUpperCase(Locale.ROOT);
            if (!ans.startsWith("S")) {
                System.out.println("Arquivo não salvo.");
                return;
            }
        }

        //tenta escrever o programa no arquivo
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"))) {
            Node c = program.getHead();
            while (c != null) {
                //chama o toString() do Node, que retorna a linha formatada
                pw.println(c);
                c = c.next;
            }
            modified = false;//marca como salvo
            currentFile = path;//atualiza o arquivo atual, se foi salvo em um caminho novo
            System.out.println("Arquivo " + quote(path) + " salvo com sucesso.");
        } catch (Exception e) {
            System.out.println("Erro ao salvar o arquivo " + quote(path) + ".");
        }
    }

    // ======= Interpretador =======

    //RUN: Inicializa e executa o interpretador do programa carregado
    private static void handleRun() {
        if (program.isEmpty()) {
            System.out.println("Erro: não há código na memória.");
            return;
        }
        //limpa e inicializa os registradores antes da execução
        for (int i = 0; i < REG_COUNT; i++) { regs[i] = 0; initialized[i] = false; }

        //Carrega o programa da Lista Encateada para arrays, para realizar execução sequencial
        int n = program.size();
        int[] lines = new int[n];//Número de linhas originais
        String[] inst = new String[n];//Instruções
        program.toArrays(lines, inst);//Copia os dados da LinkedList para o array

        int pc = 0; //Índice da instrução a ser executada no array "inst"

        //Loop principal de execução
        while (pc >= 0 && pc < n) {
            String current = inst[pc];
            int currentLine = lines[pc];
            try {
                //Executa instrução atual e verifica se houve um salto (JNZ)
                Integer jumpTo = exec(current);
                if (jumpTo != null) {
                    //JNZ: procura o índice do destino do salto no array
                    int idx = findLineIndex(lines, jumpTo);
                    if (idx == -1) {
                        //Erro: linha de salto inexistente
                        System.out.println("Erro: linha " + jumpTo + " inexistente.");
                        System.out.println("Linha: " + currentLine + " " + current);
                        return;
                    }
                    pc = idx;//Atualiza o pc para o novo índice
                } else {
                    pc++;// Se não tiver interrução/salto avança para a próxima instrução
                }
            } catch (InterpretError e) {
                //Verificação de erro de interpretação(registrador não inicializadou/divisão por zero por exemplo)
                System.out.println(e.getMessage());
                System.out.println("Linha: " + currentLine + " " + current);
                return;//Encerra a execução do programa
            }
        }
    }

    // Executa 1 instrução; retorna número de linha para saltar (jnz) ou null se for uma execuçação sequencial
    private static Integer exec(String raw) throws InterpretError {
        String[] p = raw.trim().split("\\s+");
        if (p.length == 0) return null;
        String op = p[0].toLowerCase(Locale.ROOT);

        switch (op) {
            //Lógica semelhante a do MOV, que foi implementado fora desse bloco
            case "mov" -> {
                checkArgs(op, p, 3);
                int x = regIndex(p[1], true); //Registrador do destino
                int y = value(p[2]);          //Pode ser número ou registrador (se já inicializado)
                regs[x] = y;
                initialized[x] = true;//Marca o registrador como inicializado
                return null;
            }
            case "inc" -> {
                //INC X: incrementa o valor do registrador X em 1
                checkArgs(op, p, 2);
                int x = regIndex(p[1], false);//Obtém o indíce do registrador X
                ensureInitialized(x, 'X');//Verifica se o registrador foi inicializado
                regs[x]++;//Incrementa o valor no array
                return null;
            }
            case "dec" -> {
                //DEC X: decrementa o valor do registrador X em 1
                checkArgs(op, p, 2);
                int x = regIndex(p[1], false);
                ensureInitialized(x, 'X');
                regs[x]--;//Decrementa o valor
                return null;
            }
            case "add" -> {
                //ADD X Y: adiciona o valor de Y ao valor de X (X = X + Y)
                checkArgs(op, p, 3);
                int x = regIndex(p[1], false);
                ensureInitialized(x, 'X');
                int y = value(p[2]);//Obtém o valor de Y
                regs[x] += y;//Soma
                return null;
            }
            case "sub" -> {
                //SUB X Y: subtrai o valor de Y ao de X (Y = X - Y)
                checkArgs(op, p, 3);
                int x = regIndex(p[1], false);
                ensureInitialized(x, 'X');
                int y = value(p[2]);
                regs[x] -= y;
                return null;
            }
            case "mul" -> {
                //MUL X Y: multiplica o valor de Y ao de X (Y = X * Y)
                checkArgs(op, p, 3);
                int x = regIndex(p[1], false);
                ensureInitialized(x, 'X');
                int y = value(p[2]);
                regs[x] *= y;
                return null;
            }
            case "div" -> {
                //DIV X Y: divide o valor de Y ao de X (Y = X / Y)
                checkArgs(op, p, 3);
                int x = regIndex(p[1], false);
                ensureInitialized(x, 'X');
                int y = value(p[2]);
                if (y == 0) throw new InterpretError("Erro: divisão por zero.");//Validação para evitar divisão por zero
                regs[x] /= y;
                return null;
            }
            case "jnz" -> {
                //JNZ X linha: salta para o número da linha se o valor de X for diferente de zero
                checkArgs(op, p, 3);
                int x = regIndex(p[1], false);
                ensureInitialized(x, 'X');
                int target;
                try { target = Integer.parseInt(p[2]); }
                catch (NumberFormatException e) { throw new InterpretError("Erro: linha de salto inválida."); }
                if (regs[x] != 0) return target;//Se diferente de 0, retorna a linha destino
                return null;
            }
            case "out" -> {
                //OUT X: imprime o valor do registrador X
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

    //Verifica se a quantidade de argumentos está correta para a operação
    private static void checkArgs(String op, String[] p, int expected) throws InterpretError {
        if (p.length != expected)
            throw new InterpretError("Erro: instrução " + op + " com quantidade inválida de argumentos.");
    }

    //Cria uma correlação entre o token do registrador e índice do array
    private static int regIndex(String token, boolean dest) throws InterpretError {
        if (token.length() != 1) throw new InterpretError("Erro: registrador " + token.toUpperCase(Locale.ROOT) + " inválido.");
        char ch = Character.toLowerCase(token.charAt(0));
        if (ch < 'a' || ch > 'z') throw new InterpretError("Erro: registrador " + token.toUpperCase(Locale.ROOT) + " inválido.");
        return ch - 'a';//Exemp: 'a' -> 1, 'b' -> 2, e assim em diante
    }

    //Lança um erro caso o registrador não tenha sido inicializado, com exceção para o MOV q inicia o registrador
    private static void ensureInitialized(int idx, char label) throws InterpretError {
        if (!initialized[idx]) {
            char regName = (char) ('A' + idx);
            //Mensagem que mostra qual o registrador que não foi inicializado
            throw new InterpretError("Erro: registrador " + regName + " inválido.");
        }
    }

    //Retorna o valor: podendo ser um inteiro literal ou conteúdo de um registrador
    private static int value(String tok) throws InterpretError {
        // inteiro ou registrador já inicializado
        try {
            //Se não é literal, é um registrador
            return Integer.parseInt(tok);
        } catch (NumberFormatException ignore) {
            int r = regIndex(tok, false);
            //Verifica se o registrador foi inicializado antes de retornar o valor
            if (!initialized[r]) {
                char name = (char)('A' + r);
                throw new InterpretError("Erro: registrador " + name + " inválido.");
            }
            return regs[r];//retorna o valor
        }
    }

    //Encontra o índice no array de instruções correspondente ao número da linha
    private static int findLineIndex(int[] lines, int target) {
        for (int i = 0; i < lines.length; i++) if (lines[i] == target) return i;
        return -1;//Usado caso a linha não tenha sido encontrada
    }

    //Aspas simples para ajudar na formatação
    private static String quote(String s) { return "'" + s + "'"; }

    //Formata a linha de código para melhorar a exibição
    private static String formatLine(int n, String instr) { return n + " " + instr; }

    // Exceção personalizada para controlar mensagens de erro do interpretador
    private static class InterpretError extends Exception {
        public InterpretError(String msg) { super(msg); }
    }
}
