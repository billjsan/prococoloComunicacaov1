/**
 * autor Willian J. Dos Santos
 * data 24/04/2022
 */
package server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class STTP1 {

    /**
     * constantes de controle
     */
    private final int USER_ENTRY_CODE_ERROR_NOT_VALID_NUMBER = -10;
    private final int USER_ENTRY_CODE_ERROR_IS_EMPTY = -12;
    private final int USER_ENTRY_CODE_ERROR_NOT_VALID_TEXT = -11;
    private final int USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM = -13;

    private final int USER_ENTRY_CODE_END = -14;
    private final int USER_ENTRY_CODE_BEGIN = -15;
    private final int USER_ENTRY_CODE_HELP = -16;

    private final int DIALOG_MESSAGE_END_PROGRAM = -17;
    private final int DIALOG_MESSAGE_END_TRANSFERENCE = -18;
    private final int DIALOG_MESSAGE_START_TRANSFERENCE = -19;

    private final int PORT;
    private final int DELAY_RESEND_LAST_PACKAGE;
    private final int MAX_LENGTH_PACK;
    private final int chanceToSendAnPackage;

    /**
     * dados do envio
     */
    private List<String> packagesReadyToSend = new ArrayList<>();
    private String lastPackageSentBackup = null;
    private final String DATASET;

    /**
     * variaveis de controle
     */
    private boolean isTransferring = false;
    private boolean isServerRunning = true;
    private int lastPackageSentIndex = -1;

    /**
     * temporizador
     */
    private final Timer timer = new Timer();
    private ResendPackageTask resendPackageTask;

    public STTP1(int port, int chanceToSendAnPackage, int maxLengthPack, String dataset,
                 int delayToResendLAstPackage) {

        this.MAX_LENGTH_PACK = maxLengthPack;
        this.PORT = port;
        this.chanceToSendAnPackage = chanceToSendAnPackage;
        this.DATASET = dataset;
        this.DELAY_RESEND_LAST_PACKAGE = (delayToResendLAstPackage * 1000);
    }

    /**
     * inicia o servidor Ponto de entrada da aplicação
     */
    public void startServer() {
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

                show(getHomeScreen(), output);
                if (Logger.ISLOGABLE) Logger.d("startServer", "server is running");
                while (isServerRunning) {

                    // se o usuario envou alguma coisa
                    if (input.hasNextLine()) {
                        String userInput = input.nextLine();
                        int action = getCodeFromUserEntry(userInput);

                        switch (action) {
                            case USER_ENTRY_CODE_BEGIN:
                                if (!isTransferring) {
                                    isTransferring = true;
                                    show(getDialogMessage(DIALOG_MESSAGE_START_TRANSFERENCE), output);
                                }
                                break;
                            case USER_ENTRY_CODE_END:
                                if (isTransferring) {
                                    isTransferring = false;

                                    //zera todas as variaveis de controle
                                    packagesReadyToSend = null;
                                    lastPackageSentBackup = null;
                                    lastPackageSentIndex = -1;

                                    cancelResendLastPackageSchedule();
                                    show(getDialogMessage(DIALOG_MESSAGE_END_TRANSFERENCE), output);
                                    show(getHomeScreen(), output);
                                } else {
                                    isServerRunning = false;
                                    show(getDialogMessage(DIALOG_MESSAGE_END_PROGRAM), output);
                                }
                                break;
                            case USER_ENTRY_CODE_HELP:
                                show(getHelpScreen(), output);
                                break;
                            case USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM:
                                show(getErrorMessage(USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM), output);
                                break;
                            case USER_ENTRY_CODE_ERROR_NOT_VALID_TEXT:
                                show(getErrorMessage(USER_ENTRY_CODE_ERROR_NOT_VALID_TEXT), output);
                                break;
                        }

                        if (isTransferring) {
                            String TAG = "isTransferring";

                            if (action == USER_ENTRY_CODE_BEGIN) {
                                if (Logger.ISLOGABLE) Logger.d(TAG, "send first package");
                                //prepara os pacotes para envio
                                packagesReadyToSend =
                                        addHeaderToDataset(prepareDataSet(DATASET, MAX_LENGTH_PACK),
                                                "fim do arquivo");
                                //envia o primeiro pacote
                                sendNewPackage(packagesReadyToSend.get(0), output, 0);

                            } else if (action == lastPackageSentIndex) {
                                if (Logger.ISLOGABLE) Logger.d(TAG, "resending last package");
                                resendLastPackage(output);
                            } else if (action == lastPackageSentIndex + 1
                                    && action < packagesReadyToSend.size()) {
                                if (Logger.ISLOGABLE) Logger.d(TAG, "sending next package");
                                cancelResendLastPackageSchedule();
                                sendNewPackage(packagesReadyToSend.get(action), output, action);
                            } else if (action == packagesReadyToSend.size()) {
                                if (Logger.ISLOGABLE) Logger.d(TAG, "end of file");
                                show("o dado acabou", output);
                                isTransferring = false;
                                cancelResendLastPackageSchedule();
                                show(getHomeScreen(), output);
                            } else {
                                if (Logger.ISLOGABLE) Logger.d(TAG, "no match param");
                                show(getErrorMessage(USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM), output);
                            }
                        }
                    }
                }

            } finally {
                if (Logger.ISLOGABLE) Logger.d("startServer", "server was closed");
                socket.close();
            }
        } catch (IOException e) {
            if (Logger.ISLOGABLE) Logger.d("startServer", "server could not start");
            e.printStackTrace();
        }
    }

    private void sendNewPackage(String newPackage, PrintWriter output, int index) {
        String TAG = "sendNewPackage";

        lastPackageSentBackup = newPackage;
        lastPackageSentIndex = index;

        if (shouldSentAnPackage() <= chanceToSendAnPackage) {
            if (Logger.ISLOGABLE) Logger.d(TAG, "enviando pacote: " + newPackage);

            output.println(newPackage); //simular perda de pacote
        } else {
            if (Logger.ISLOGABLE) Logger.d(TAG, "pacote perdido: " + newPackage);
        }
        resendLastPackageSchedule(newPackage, output);
    }

    private int shouldSentAnPackage() {
        Random random = new Random();
        return random.nextInt(100);
    }

    private void resendLastPackageSchedule(String lastPackage, PrintWriter output) {
        String TAG = "resendLastPackageSchedule";

        if (Logger.ISLOGABLE) Logger.d(TAG, "agendando reenvio de pacote: " + lastPackage);

        resendPackageTask = new ResendPackageTask(lastPackage, output);
        timer.schedule(resendPackageTask, DELAY_RESEND_LAST_PACKAGE);
    }

    private void cancelResendLastPackageSchedule() {
        String TAG = "cancelResendLastPackageSchedule";

        if (Logger.ISLOGABLE) Logger.d(TAG, "cancelando reenvio de pacote");
        resendPackageTask.cancel();
    }

    private void resendLastPackage(PrintWriter output) {
        output.println(lastPackageSentBackup);
    }

    private int getCodeFromUserEntry(String userInput) {
        String TAG = "getCodeFromUserEntry";
        if (!isTransferring) {//not transferring yet
            if (Logger.ISLOGABLE) Logger.d(TAG, "not transferring");
            switch (userInput.trim()) {
                case "begin":
                    if (Logger.ISLOGABLE) Logger.d(TAG, "USER_ENTRY_CODE_BEGIN");
                    return USER_ENTRY_CODE_BEGIN;

                case "end":
                    if (Logger.ISLOGABLE) Logger.d(TAG, "USER_ENTRY_CODE_END");
                    return USER_ENTRY_CODE_END;

                case "help":
                    if (Logger.ISLOGABLE) Logger.d(TAG, "USER_ENTRY_CODE_HELP");
                    return USER_ENTRY_CODE_HELP;

                default:
                    if (Logger.ISLOGABLE) Logger.d(TAG, "USER_ENTRY_CODE_ERROR_NOT_VALID_TEXT");
                    return USER_ENTRY_CODE_ERROR_NOT_VALID_TEXT;
            }
        } else {//is transferring
            if (Logger.ISLOGABLE) Logger.d(TAG, "is transferring");

            if (userInput.equals("end")) {
                if (Logger.ISLOGABLE) Logger.d(TAG, "USER_ENTRY_CODE_END");
                return USER_ENTRY_CODE_END;

            } else if (userInput.trim().equals("help")) {
                if (Logger.ISLOGABLE) Logger.d(TAG, "USER_ENTRY_CODE_HELP");
                return USER_ENTRY_CODE_HELP;

            } else {
                try {
                    int value = Integer.parseInt(userInput);
                    if (Logger.ISLOGABLE) Logger.d(TAG, "user input integer value: " + value);
                    return value;
                } catch (Exception e) {
                    if (Logger.ISLOGABLE) Logger.d(TAG, "error to convert into integer");
                }
            }
        }

        if (Logger.ISLOGABLE) Logger.d(TAG, "USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM user input: " + userInput);
        return USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM;
    }

    private void show(String message, PrintWriter output) {
        output.println(message);
    }

    private List<String> prepareDataSet(String dataToTransfer, int maxLengthPack) {

        char[] datasetInCharArray = dataToTransfer.toCharArray();
        StringBuilder pack = new StringBuilder();
        List<String> packages = new ArrayList<>();
        int lastCharIndex = 0;
        boolean isReady = false;

        while (!isReady) {

            if (pack.length() >= maxLengthPack) {
                packages.add(pack.toString());
                pack = new StringBuilder();
            }
            if (lastCharIndex >= datasetInCharArray.length) {
                packages.add(pack.toString());
                pack = new StringBuilder();
                isReady = true;
            } else {
                pack.append(datasetInCharArray[lastCharIndex]);
                lastCharIndex++;
            }
        }
        return packages;
    }

    private List<String> addHeaderToDataset(List<String> packageFormatted,
                                            String endOfFileMessage) {
        List<String> packageWithHeader = new ArrayList<>();

        int i;
        for (i = 0; i < packageFormatted.size(); i++) {

            String pack = packageFormatted.get(i);
            packageWithHeader.add("[" + i + "]" + "[" + pack.length() + "]" + "[*]" + pack);
        }
        packageWithHeader.add("[" + (i) + "]" + "[" + endOfFileMessage.length() + "]" + "[*]" +
                endOfFileMessage);
        return packageWithHeader;
    }

    private String getDialogMessage(int dialog) {
        String TAG = "getDialogMessage";
        String dialogMessage = "";

        if (dialog == DIALOG_MESSAGE_END_PROGRAM) {
            if (Logger.ISLOGABLE) Logger.d(TAG, "DIALOG_MESSAGE_END_PROGRAM");
            dialogMessage += "\n";
            dialogMessage += "--------------------------------------------------------------------------------------------------";
            dialogMessage += "\n";
            dialogMessage += "------------------------------ Encerrando o Programa ---------------------------------------------";
            dialogMessage += "\n";
            dialogMessage += "-------------------------- obrigado por usar o STTP 1.0 ------------------------------------------";
            dialogMessage += "\n";
            dialogMessage += "--------------------------------------------------------------------------------------------------";
            dialogMessage += "\n";
            return dialogMessage;

        } else if (dialog == DIALOG_MESSAGE_END_TRANSFERENCE) {
            if (Logger.ISLOGABLE) Logger.d(TAG, "DIALOG_MESSAGE_END_TRANSFERENCE");
            dialogMessage += "\n";
            dialogMessage += "--------------------------------------------------------------------------------------------------";
            dialogMessage += "\n";
            dialogMessage += "---------------------------- Encerrando a Transferência ------------------------------------------";
            dialogMessage += "\n";
            dialogMessage += "--------------------------------------------------------------------------------------------------";
            dialogMessage += "\n";
            return dialogMessage;

        } else if (dialog == DIALOG_MESSAGE_START_TRANSFERENCE) {
            if (Logger.ISLOGABLE) Logger.d(TAG, "DIALOG_MESSAGE_START_TRANSFERENCE");
            dialogMessage += "\n";
            dialogMessage += "--------------------------------------------------------------------------------------------------";
            dialogMessage += "\n";
            dialogMessage += "----------------------------- Iniciando a Transferência ------------------------------------------";
            dialogMessage += "\n";
            dialogMessage += "--------------------------------------------------------------------------------------------------";
            dialogMessage += "\n";
            return dialogMessage;
        }
        return dialogMessage;
    }

    private String getErrorMessage(int error) {
        String TAG = "getErrorMessage";
        String errorMessage = "";
        if (error == USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM) {
            if (Logger.ISLOGABLE)
                Logger.d(TAG, "USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM " + USER_ENTRY_CODE_ERROR_NO_MATCH_PARAM);
            errorMessage += "\n";
            errorMessage += "--------------------------------------------------------------------------------------------------";
            errorMessage += "\n";
            errorMessage += "------------------------------ voce digitou um dado invalido -------------------------------------";
            errorMessage += "\n";
            errorMessage += "--------------------------------------------------------------------------------------------------";
            return errorMessage;
        } else if (error == USER_ENTRY_CODE_ERROR_IS_EMPTY) {//not used yet
            if (Logger.ISLOGABLE) Logger.d(TAG, "USER_ENTRY_CODE_ERROR_IS_EMPTY " + USER_ENTRY_CODE_ERROR_IS_EMPTY);
            errorMessage += "\n";
            errorMessage += "--------------------------------------------------------------------------------------------------";
            errorMessage += "\n";
            errorMessage += "----------------------------------- voce nao digitou nada ----------------------------------------";
            errorMessage += "\n";
            errorMessage += "--------------------------------------------------------------------------------------------------";
            return errorMessage;
        } else if (error == USER_ENTRY_CODE_ERROR_NOT_VALID_TEXT) {
            if (Logger.ISLOGABLE)
                Logger.d(TAG, "USER_ENTRY_CODE_ERROR_NOT_A_TEXT " + USER_ENTRY_CODE_ERROR_NOT_VALID_TEXT);
            errorMessage += "\n";
            errorMessage += "--------------------------------------------------------------------------------------------------";
            errorMessage += "\n";
            errorMessage += "--------------------------- voce nao digitou um texto valido -------------------------------------";
            errorMessage += "\n";
            errorMessage += "--------------------------------------------------------------------------------------------------";
            return errorMessage;
        } else if (error == USER_ENTRY_CODE_ERROR_NOT_VALID_NUMBER) {//not used yet
            if (Logger.ISLOGABLE) Logger.d(TAG, "INTERN_ERROR_LAST_PACKAGE_INDEX_NOT_A_NUMBER "
                    + USER_ENTRY_CODE_ERROR_NOT_VALID_NUMBER);
            errorMessage += "\n";
            errorMessage += "--------------------------------------------------------------------------------------------------";
            errorMessage += "\n";
            errorMessage += "------------------------------- voce nao digitou um numero ---------------------------------------";
            errorMessage += "\n";
            errorMessage += "--------------------------------------------------------------------------------------------------";
            return errorMessage;
        }
        if (Logger.ISLOGABLE) Logger.d(TAG, "unknown error code: " + error);
        return null;
    }

    private String getHomeScreen() {
        String helpScreen = "";
        helpScreen += "--------------------------------------------------------------------------------------------------";
        helpScreen += "\n";
        helpScreen += "---------------------------------- Bem-vindo ao STTP 1.0 -----------------------------------------";
        helpScreen += "\n";
        helpScreen += "------ Este é um protocolo de comunicação  de alto nível utilizando SocketServer e Telnet --------";
        helpScreen += "\n";
        helpScreen += "------ foi desenvolvido  como  exercicio da disciplina de  Sistemas Distribuídos do curso --------";
        helpScreen += "\n";
        helpScreen += "------ de análise e desenvolvimento de sistemas do IFPE no ano de 2020, sua implementação --------";
        helpScreen += "\n";
        helpScreen += "------ e utilização tem fins meramente didáticos não  tendo como objetivo o uso comercial --------";
        helpScreen += "\n";
        helpScreen += "------         para descobrir com usar digite [help] ou [end] para encerrar               --------";
        helpScreen += "\n";
        helpScreen += "--------------------------------------------------------------------------------------------------";
        helpScreen += "\n";
        return helpScreen;
    }

    private String getHelpScreen() {

        String homeScreen = "";
        homeScreen += "------------------------------------------ HELP --------------------------------------------------";
        homeScreen += "\n";
        homeScreen += "|     O STTP é um protocolo de transferência de arquivo de texto de alto nível acima do TCP       |";
        homeScreen += "\n";
        homeScreen += "|     seu uso é  extremamente simples. Para iniciar  a transferencia basta digitar [begin].       |";
        homeScreen += "\n";
        homeScreen += "|     O servidor irá iniciar a transferência  enviado o primeiro pacote no formato abaixo:        |";
        homeScreen += "\n";
        homeScreen += "|                        [0][30][*][aqui está o conteudo do pacote]                               |";
        homeScreen += "\n";
        homeScreen += "|     O primeiro campo indica o índice do arquivo  que é de ordem crescete até a conclusão        |";
        homeScreen += "\n";
        homeScreen += "|     O segundo campo indica o tamanho de caracteres que está sendo enviado pelo  servidor        |";
        homeScreen += "\n";
        homeScreen += "|     O terceiro campo está reservado para uso futuro e não tem uso no momento                    |";
        homeScreen += "\n";
        homeScreen += "|     O último campo é o dado que está sendo transferido. Ao término o servidor informará         |";
        homeScreen += "\n";
        homeScreen += "|     Que o dado foi totalmente enviado.                                                          |";
        homeScreen += "\n";
        homeScreen += "|     Para receber o próximo pacote o usuário deve digitar o número do próximo indice que         |";
        homeScreen += "\n";
        homeScreen += "|     é esperado ou digitar o último índice recebido para que o servidor o reenvie.               |";
        homeScreen += "\n";
        homeScreen += "|     O servidor irá reenviar automaticamento o último dado após 15 segundos de inatividade       |";
        homeScreen += "\n";
        homeScreen += "|     A qualquer momendo o usuário pode digitar [end] e a transferencia será encerrada            |";
        homeScreen += "\n";
        homeScreen += "--------------------------------------------------------------------------------------------------";
        homeScreen += "\n";

        return homeScreen;
    }

    private static class ResendPackageTask extends TimerTask {


        private final String TAG = ResendPackageTask.class.getSimpleName();
        private final String lastPackageSent;
        PrintWriter output;

        public ResendPackageTask(String lastPackageSent, PrintWriter output) {
            this.lastPackageSent = lastPackageSent;
            this.output = output;
        }

        @Override
        public void run() {
            if (Logger.ISLOGABLE) Logger.d(TAG, "reenviando pacote: " + lastPackageSent);
            output.println(lastPackageSent);
        }
    }
}
