package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Serv {

    private static final String TAG = Serv.class.getSimpleName();
    private final int PORT;
    private final int chanceDeEnviarUmPacote;

    private String ultimoPacoteEnviado = null; //lembrar de resetar valor quando acabar transmissao

    /**
     * constantes de controle
     */
    public static final int INTERN_ERROR_LAST_PACKAGE_INDEX_NOT_A_NUMBER = -222222222;
    public static final int USER_ENTRY_CODE_ERROR_NOT_A_TEXT = -33333333;
    public static final int USER_ENTRY_CODE_ERROR_IS_EMPTY = -11111111;
    public static final int USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM = -99999999;

    public static final int USER_ENTRY_CODE_END = -5516546;
    public static final int USER_ENTRY_CODE_BEGIN = -55618155;
    public static final int USER_ENTRY_CODE_HELP = -55754848;

    private boolean run = true;
    private int userIndex = -1;
    private boolean isTransferring = false;

    private String DADOS_PARA_ENVIO = "hello i love you let me jump in your game hello i love you let me jump in your game hello i love you let me jump in your game hello i love you let me jump in your game hello i love you let me jump in your game hello i love you let me jump in your game hello i love you let me jump in your game hello i love you let me jump in your game hello i love you let me jump in your game ";
    private int TAMANHO_MAX_PACOTE;
    private List<String> pacotesParaEnvio = new ArrayList<>();
    private int indexLastPackageSent = -1;

    public Serv(int porta, int chanceDeEnviarUmPacote, int tamanhoMaxPacote){

        this.TAMANHO_MAX_PACOTE = tamanhoMaxPacote;
        this.PORT = porta;
        this.chanceDeEnviarUmPacote = chanceDeEnviarUmPacote;
    }

    private String getUltimoPacoteEnviado() {
        return ultimoPacoteEnviado;
    }

    private void setUltimoPacoteEnviado(String ultimoPacoteEnviado) {
        this.ultimoPacoteEnviado = ultimoPacoteEnviado;
    }

    /**
     * inicia o servidor. Ponto de entrada da aplicação
     */
    public void startServer(){

        Scanner input;
        PrintWriter output;


        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            Socket socket = serverSocket.accept();
            try {
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();

                input = new Scanner(inputStream);
                output = new PrintWriter(outputStream, true);

                show(getHomeScreen(),output);
                //status da aplicacao
                while (run) {


                    // se o usuario envou alguma coisa
                    if (input.hasNextLine()) {
                        String userInput = input.nextLine();
                        int userInputsToConstants = getUserInputsToConstants(userInput);
                        executefromConstants(userInputsToConstants, output, input);

                        //downloading ocorrendo
                        if(isTransferring){
                            if(userIndex == -1){
                                output.println(pacotesParaEnvio.get(0));
                            }else {
                                output.println(pacotesParaEnvio.get(0));
                            }
                        }
                    }
                }

            } finally {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void executefromConstants(int value, PrintWriter output, Scanner input){

        String TAG = "executefromConstants";
        switch (value){
            case USER_ENTRY_CODE_END:
                if (Logger.ISLOGABLE)Logger.d(TAG, "USER_ENTRY_CODE_END: " + USER_ENTRY_CODE_END);
                userIndex = -1;
                ultimoPacoteEnviado = null;
                isTransferring = false;
                indexLastPackageSent = -1;
                show("Encerrado pelo usuario", output);
                run = false;
                break;
            case USER_ENTRY_CODE_HELP:
                if (Logger.ISLOGABLE)Logger.d(TAG, "USER_ENTRY_CODE_HELP: " + USER_ENTRY_CODE_HELP);

                show(getHelpScreen(), output);
                break;
            case USER_ENTRY_CODE_BEGIN:
                if (Logger.ISLOGABLE)Logger.d(TAG, "USER_ENTRY_CODE_BEGIN: " + USER_ENTRY_CODE_BEGIN);

                if(!isTransferring){
                    isTransferring = true;
                    pacotesParaEnvio = adicionaCabecalhos(preparaPacotes(DADOS_PARA_ENVIO, TAMANHO_MAX_PACOTE),
                            "fim do arquivo");
                }

                //startTransferring(output, input);
                break;
            case USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM:
                if (Logger.ISLOGABLE)Logger.d(TAG, "USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM: "
                        + USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM);

                show(getErrorMessage(USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM), output);
                break;
            case USER_ENTRY_CODE_ERROR_IS_EMPTY:
                if (Logger.ISLOGABLE)Logger.d(TAG, "USER_ENTRY_CODE_ERROR_IS_EMPTY: "
                        + USER_ENTRY_CODE_ERROR_IS_EMPTY);

                show(getErrorMessage(USER_ENTRY_CODE_ERROR_IS_EMPTY), output);
                break;
            case USER_ENTRY_CODE_ERROR_NOT_A_TEXT:
                if (Logger.ISLOGABLE)Logger.d(TAG, "USER_ENTRY_CODE_ERROR_NOT_A_TEXT: "
                        + USER_ENTRY_CODE_ERROR_NOT_A_TEXT);

                show(getErrorMessage(USER_ENTRY_CODE_ERROR_NOT_A_TEXT), output);
                break;
            case INTERN_ERROR_LAST_PACKAGE_INDEX_NOT_A_NUMBER:
                if (Logger.ISLOGABLE)Logger.d(TAG, "INTERN_ERROR_LAST_PACKAGE_INDEX_NOT_A_NUMBER: "
                        + INTERN_ERROR_LAST_PACKAGE_INDEX_NOT_A_NUMBER);

                show(getErrorMessage(INTERN_ERROR_LAST_PACKAGE_INDEX_NOT_A_NUMBER), output);
                break;
            default:
                userIndex = value;

        }
    }

    private void startTransferring(PrintWriter output, Scanner input) {
        List<String> strings = adicionaCabecalhos(preparaPacotes(DADOS_PARA_ENVIO,
                50), "Fim do arquivo");

        //esse é o envio do primeiro pacote
        if (ultimoPacoteEnviado == null){
            output.println(strings.get(0));
            ultimoPacoteEnviado = strings.get(0);
            //shcedula o timer
        }

        if (input.hasNextLine()){
            String string = input.nextLine();
            executefromConstants(getUserInputsToConstants(string), output, input);

            if(userIndex != -1 && userIndex <= strings.size()+1){
                output.println(strings.get(userIndex));
                ultimoPacoteEnviado = strings.get(userIndex);
            }
        }
    }

    private String getErrorMessage(int error) {
        String TAG = "getErrorMessage";
        String errorMessage = "";
        if(error == USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM){
            if (Logger.ISLOGABLE) Logger.d(TAG,"USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM "+ USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM);
            errorMessage +="\n";
            errorMessage += "--------------------------------------------------------------------------------------------------";
            errorMessage +="\n";
            errorMessage += "------------------------------ voce digitou um dado invalido -------------------------------------";
            errorMessage +="\n";
            errorMessage += "--------------------------------------------------------------------------------------------------";
            return errorMessage;
        }
        else if (error == USER_ENTRY_CODE_ERROR_IS_EMPTY){
            if (Logger.ISLOGABLE) Logger.d(TAG,"USER_ENTRY_CODE_ERROR_IS_EMPTY "+ USER_ENTRY_CODE_ERROR_IS_EMPTY);
            errorMessage +="\n";
            errorMessage += "--------------------------------------------------------------------------------------------------";
            errorMessage +="\n";
            errorMessage += "----------------------------------- voce nao digitou nada ----------------------------------------";
            errorMessage +="\n";
            errorMessage += "--------------------------------------------------------------------------------------------------";
            return errorMessage;
        }
        else if (error == USER_ENTRY_CODE_ERROR_NOT_A_TEXT) {
            if (Logger.ISLOGABLE) Logger.d(TAG,"USER_ENTRY_CODE_ERROR_NOT_A_TEXT "+ USER_ENTRY_CODE_ERROR_NOT_A_TEXT);
            errorMessage +="\n";
            errorMessage += "--------------------------------------------------------------------------------------------------";
            errorMessage +="\n";
            errorMessage += "--------------------------- voce nao digitou um texto valido -------------------------------------";
            errorMessage +="\n";
            errorMessage += "--------------------------------------------------------------------------------------------------";
            return errorMessage;
        }
        else if (error == INTERN_ERROR_LAST_PACKAGE_INDEX_NOT_A_NUMBER) {
            if (Logger.ISLOGABLE) Logger.d(TAG,"INTERN_ERROR_LAST_PACKAGE_INDEX_NOT_A_NUMBER "
                    + INTERN_ERROR_LAST_PACKAGE_INDEX_NOT_A_NUMBER);
            errorMessage +="\n";
            errorMessage += "--------------------------------------------------------------------------------------------------";
            errorMessage +="\n";
            errorMessage += "------------------------------- voce nao digitou um numero ---------------------------------------";
            errorMessage +="\n";
            errorMessage += "--------------------------------------------------------------------------------------------------";
            return errorMessage;
        }
        if (Logger.ISLOGABLE) Logger.d(TAG,"unknown error code: "+ error);
        return null;
    }

    private String getHomeScreen() {
        String helpScreen="";
        helpScreen += "--------------------------------------------------------------------------------------------------";
        helpScreen +="\n";
        helpScreen += "------------------------------------ Welcome to STTP 1.0 -----------------------------------------";
        helpScreen +="\n";
        helpScreen += "|                      To start the download enter \"begin\"                                       |";
        helpScreen +="\n";
        helpScreen += "|   The data will be received with a header who is inform the correct receiving sequence         |";
        helpScreen +="\n";
        helpScreen += "|             something like this: [0][255][*][this is the data received]                        |";
        helpScreen +="\n";
        helpScreen += "|    the first field indicates the sequence of the data and you must enter the next natural      |";
        helpScreen +="\n";
        helpScreen += "|    number before 10 sec timeout the second field indicates the size of the data received       |";
        helpScreen +="\n";
        helpScreen += "|                   if you enter \"end\" the connection will be killed                             |";
        helpScreen +="\n";
        helpScreen += "--------------------------------------------------------------------------------------------------";
        helpScreen +="\n";
        return helpScreen;
    }

    private void show(String homeScreen, PrintWriter output) {
        output.println(homeScreen);
    }

    private String getHelpScreen() {

        String homeScreen="";
        homeScreen += "----------------------------------------- HELP ---------------------------------------------------";
        homeScreen +="\n";
        homeScreen += "------------------------------------ Welcome to STTP 1.0 -----------------------------------------";
        homeScreen +="\n";
        homeScreen += "|                      To start the download enter \"begin\"                                       |";
        homeScreen +="\n";
        homeScreen += "|   The data will be received with a header who is inform the correct receiving sequence         |";
        homeScreen +="\n";
        homeScreen += "|             something like this: [0][255][*][this is the data received]                        |";
        homeScreen +="\n";
        homeScreen += "|    the first field indicates the sequence of the data and you must enter the next natural      |";
        homeScreen +="\n";
        homeScreen += "|    number before 10 sec timeout the second field indicates the size of the data received       |";
        homeScreen +="\n";
        homeScreen += "|                   if you enter \"end\" the connection will be killed                             |";
        homeScreen +="\n";
        homeScreen += "--------------------------------------------------------------------------------------------------";
        homeScreen +="\n";
        return homeScreen;
    }

    /**
     *
     * @param userInput recebe um valor qualquer do usuario e transfora em um
     *                inteiro sendo entendido pelo programador utilizando as
     *                constantes de controle
     *
     * @return uma constante inteira entendida pelo sistema ou recebe o
     * índice do proximo pacote a ser transmitido
     */
    public int getUserInputsToConstants(String userInput){

        String TAG = "getUserInputsToConstants";

//         VERIFICA SE ESTA TRANSMITINDO
//         verifica se algum pacote ja foi enviadodo indicando que a userInput
//         do usuario pode ser um numero de sequencia e tenta converter  o
//         indice do ultimo cabecalho enviado para um inteiro
        if (!(getUltimoPacoteEnviado() == null ) && !(getUltimoPacoteEnviado().isEmpty())){
            String substring = getUltimoPacoteEnviado().substring(1, 2);

            try {
                indexLastPackageSent = Integer.parseInt(substring);
                if (Logger.ISLOGABLE) Logger.d(TAG, "last index: " +indexLastPackageSent);

            }catch (Exception e){

                if (Logger.ISLOGABLE){
                    Logger.d(TAG, "index conversion to int failure");
                }
                return INTERN_ERROR_LAST_PACKAGE_INDEX_NOT_A_NUMBER;
            }
        }

//         NAO ESTA TRANSMITINDO
//         se @indexLastPackageSent for = -1 significa que nenhum pacote
//         foi enviado ainda e significa que a entreda do usuario precisa
//         ser um texto
        if(indexLastPackageSent == -1){

            if(userInput == null || userInput.isEmpty()){
                return USER_ENTRY_CODE_ERROR_IS_EMPTY;
            }

            if(Logger.ISLOGABLE) Logger.d(TAG, "user entry: " +userInput);

//         possiveis entradas de texto do usuario
            switch (userInput.trim()){
                case "end":
                    return USER_ENTRY_CODE_END;
                case "begin":
                    return USER_ENTRY_CODE_BEGIN;
                case "help":
                    return USER_ENTRY_CODE_HELP;
                default:
                    return USER_ENTRY_CODE_ERROR_NOT_A_TEXT;
            }
        }

//              ESTA TRANSMITINDO
//         nesse caso ou o usuario entra com o indice do ultimo pacote enviado
//         ou entra com o indice do proximo pacote a ser enviado ou entra com o
//         texto "end" para finalizar a operacao
//
        if (indexLastPackageSent >= 0){

            if(userInput == null || userInput.isEmpty()){
                return USER_ENTRY_CODE_ERROR_IS_EMPTY;
            }
            if(Logger.ISLOGABLE) Logger.d(TAG, "user entry: " +userInput);

            if(userInput.trim().equals("end")){
                return USER_ENTRY_CODE_END;
            }else {
                try {
                    int userInputIntValue = Integer.parseInt(userInput);

                    if (userInputIntValue == indexLastPackageSent){
                        return indexLastPackageSent;

                    } else if (userInputIntValue == indexLastPackageSent+1) {
                        return indexLastPackageSent +1;
                    }else {
                        return USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM;
                    }
                }catch (Exception e){

                    if(Logger.ISLOGABLE) Logger.d(TAG, "user entry no match param");
                    return USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM;
                }
            }
        }
        return USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM;
    }

    public List<String> preparaPacotes(String dadosParaTransferencia, int tamanhoMaxDoPacote) {

        char[] dadosEmChar = dadosParaTransferencia.toCharArray();
        StringBuilder pacotinho = new StringBuilder();
        List<String> meusPacotinhos = new ArrayList<>();
        int indiceUltimoChar = 0;
        boolean pronto = false;

        while (!pronto) {

            if (pacotinho.length() >= tamanhoMaxDoPacote) {
                meusPacotinhos.add(pacotinho.toString());
                pacotinho = new StringBuilder();
            }
            if (indiceUltimoChar >= dadosEmChar.length) {
                meusPacotinhos.add(pacotinho.toString());
                pacotinho = new StringBuilder();
                pronto = true;
            } else {
                pacotinho.append(dadosEmChar[indiceUltimoChar]);
                indiceUltimoChar++;
            }
        }
        return  meusPacotinhos;
    }

    public List<String> adicionaCabecalhos(List<String> pacotesProntosParaTranferir,
                                           String mensagemFimDoArquivo) {
        List<String> dadosComCabecalho = new ArrayList<>();

        int i;
        for (i = 0; i < pacotesProntosParaTranferir.size(); i++) {

            String pacote = pacotesProntosParaTranferir.get(i);
            dadosComCabecalho.add("[" + i + "]" + "[" + pacote.length() + "]" + "[*]" + pacote);
        }
        dadosComCabecalho.add("[" + (i) + "]" + "[" + mensagemFimDoArquivo.length() + "]" + "[*]" +
                mensagemFimDoArquivo);
        return dadosComCabecalho;
    }
}
