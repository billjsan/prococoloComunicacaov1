package client.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame {

    private final String TITULO_APP = "STTP - Cliente";
    private String TEXTO_BOTAO = "ir";
    private JTextArea editTextIP;
    private JTextArea editTextPorta;

    private String PORT;
    private String IP;

    private String pageContent;

    private JTextField pageContentField;
    private JPanel painelDeConteudo;

    boolean userSetAddress = false;

    public MainWindow() throws HeadlessException {
        super();
        createWindow();
    }

    public boolean hasUserSetAddress(){
        return userSetAddress;
    }

    public void setPageContent(String content){
        this.pageContent = content;
    }

    public String getIP(){
        return IP;
    }

    public String getPORT(){
        return PORT;
    }

    private void createWindow() {

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(1000, 700));
        this.setLocation(getWidth() / 2, getHeight() / 3);
        this.setTitle(TITULO_APP);
        Container window = getContentPane();
        this.setLayout(new BoxLayout(window, BoxLayout.Y_AXIS));

        //cria painel de titulo
        JPanel painelDeTitulo = new JPanel();
        painelDeTitulo.setLayout(new BoxLayout(painelDeTitulo, BoxLayout.X_AXIS));

        //cria o texto para o painel de titulo
        JLabel textoDoCabecalho = new JLabel("cabeçalho");
        textoDoCabecalho.setFont(new Font("default", Font.BOLD, 30));

        //cria painel de menus
        JPanel painelDeMenu = new JPanel();
        BoxLayout boxLayout = new BoxLayout(painelDeMenu, BoxLayout.X_AXIS);
        painelDeMenu.setLayout(boxLayout);

        //cria painel do conteudo
        painelDeConteudo = new JPanel();
        painelDeConteudo.setLayout(new BoxLayout(painelDeConteudo, BoxLayout.X_AXIS));


//        pageContentField = new JTextField();
//        pageContentField.setColumns(10);
//        pageContentField.setText("");
//        pageContentField.setEditable(false);
//        painelDeConteudo.add(pageContentField);

        //titulo texto do ip
        JLabel textViewIP = new JLabel();
        textViewIP.setFont(new Font("default", Font.PLAIN, 25));
        textViewIP.setText("IP ");

        //pega texto do ip
        editTextIP = new JTextArea();
        editTextIP.setMaximumSize(new Dimension(180,20));
        editTextIP.setFont(new Font("default", Font.ITALIC, 20));
        editTextIP.setRequestFocusEnabled(true);

        //titulo texto porta
        JLabel textViewPorta = new JLabel();
        textViewPorta.setFont(new Font("default", Font.PLAIN, 25));
        textViewPorta.setText(" Porta ");

        //pega texto porta
        editTextPorta = new JTextArea();
        editTextPorta.setMaximumSize(new Dimension(60,20));
        editTextPorta.setFont(new Font("default", Font.ITALIC, 20));

        //cria botao
        TEXTO_BOTAO = "ir";
        JButton botaoIr = new JButton(TEXTO_BOTAO);
        botaoIr.setMaximumSize(new Dimension(50,30));
        botaoIr.setToolTipText(TEXTO_BOTAO);

        //painel de titulo
        painelDeTitulo.add(textoDoCabecalho);

        //painel de menu
        painelDeMenu.add(textViewIP);
        painelDeMenu.add(editTextIP);
        painelDeMenu.add(textViewPorta);
        painelDeMenu.add(editTextPorta);
        painelDeMenu.add(botaoIr);

        window.add(painelDeTitulo);
        window.add(painelDeMenu);
        window.add(painelDeConteudo);

        SetClickListener botaoHandler = new SetClickListener();
        botaoIr.addActionListener(botaoHandler);

        this.setVisible(true);
    }

    public void showPageContent() {
        pageContentField = new JTextField();
        pageContentField.setColumns(10);
        pageContentField.setText("lalalala");
        pageContentField.setEditable(false);
        painelDeConteudo.add(pageContentField);
    }

    private class SetClickListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent event) {

            IP = editTextIP.getText();
            PORT = editTextPorta.getText();
            userSetAddress = true;
        }
    }
}
