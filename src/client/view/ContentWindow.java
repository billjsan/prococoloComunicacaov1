package client.view;

import client.MyLogger;

import javax.swing.*;
import java.awt.*;

public class ContentWindow {

    private final String TAG = ContentWindow.class.getSimpleName();
    private final String content;

    public ContentWindow(String content) {
        this.content = content;
    }

    public void show() {
        if (MyLogger.ISLOGABLE) MyLogger.d(TAG, "show");
        JFrame frame = new JFrame();
        frame.setSize(new Dimension(1000, 300));
        frame.setLocation(frame.getWidth() / 2, frame.getHeight());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextPane jTextPane = new JTextPane();
        jTextPane.setFont(new Font("default", Font.PLAIN, 30));
        frame.getContentPane().add(BorderLayout.CENTER, jTextPane);
        jTextPane.setContentType("text/html");

        String style = "font-size: 16px;" +
                "text-align: center;";

        jTextPane.setText("<html><head><style>body{" + style + "}</style></head><body>" + formatContentToHTML(content)
                + "</body></html>");

        frame.setVisible(true);
    }

    private String formatContentToHTML(String content) {
        if (MyLogger.ISLOGABLE) MyLogger.d(TAG, "formatContentToHTML");
        String a = content.replace("<:>", "<br>");
        String b = a.replace("<$b>", "<b>");
        String c = b.replace("<#b>", "</b>");
        String d = c.replace("<$i>", "<i>");
        return d.replace("<#i>", "</i>");
    }

}
