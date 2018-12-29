import javax.mail.*;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Properties;

import static javax.swing.JOptionPane.showMessageDialog;

public class MailReceiver extends JFrame
{
    private JTextField server;
    private JTextField user;
    private JTextField password;
    private JTextArea emailPresenter;

    public static void main(String[] paramArrayOfString) {
        new MailReceiver();
    }

    public MailReceiver() {

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(500, 400));
        setLayout(new BorderLayout());

        JPanel emailInformation = new JPanel();
        emailInformation.setLayout(new GridLayout(3,2));
        emailInformation.add(new JLabel("Server: "));
        emailInformation.add(server = new JTextField());
        emailInformation.add(new JLabel("Username: "));
        emailInformation.add(user = new JTextField());
        emailInformation.add(new JLabel("Password: "));
        emailInformation.add(password = new JTextField());

        add(emailInformation, BorderLayout.NORTH);

        emailPresenter = new JTextArea();
        emailPresenter.setBackground(Color.WHITE);
        add(new JScrollPane(emailPresenter), BorderLayout.CENTER);

        JButton getEmail = new JButton("Receive emails");
        getEmail.addActionListener(e -> {
            emailPresenter.setText("");
            receiveEmails(server.getText(), user.getText(), password.getText());
        });
        add(getEmail, BorderLayout.SOUTH);

        setVisible(true);
        pack();
    }

    public void receiveEmails(String server, String user, String password){

        try {
            Properties properties = new Properties();
            properties.put("mail.pop3.host", server);
            properties.put("mail.pop3.socketFactory.class" , "javax.net.ssl.SSLSocketFactory" );

            Session session = Session.getDefaultInstance(properties);
            Store store = session.getStore("pop3");
            store.connect(server, 995, user, password);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] emails = inbox.getMessages();
            printEmails(emails);

            inbox.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
            showMessageDialog(this, "Something went wrong, try again!");
            e.printStackTrace();
        } catch (MessagingException e) {
            if(e.getMessage().contains("Username and password not accepted")){
                showMessageDialog(this, "Authentication failed");
            }else {
                showMessageDialog(this, "Something went wrong, try again!");
            }
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showMessageDialog(this, "Something went wrong, try again!");
            e.printStackTrace();
        }
    }

    private String readMessage(Message message){
        String newMessage = "";

        try {
            if(message.isMimeType("text/plain")){
                return message.getContent().toString();
            }
            if(message.isMimeType("multipart/*")){
                newMessage += "The message containt attached files \n";

                Multipart multipart = (Multipart) message.getContent();
                for(int i = 0; i < multipart.getCount(); i++){
                    Part part = multipart.getBodyPart(i);
                    if(part.isMimeType("text/plain")){
                        newMessage += part.getContent().toString() + "\n";
                    }
                }
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newMessage;
    }

    private void printEmails(Message[] emails){
        try {
            for (int i = 0; i < emails.length; i++) {
                Message m = emails[i];
                emailPresenter.append("\nMessage: " + m.getMessageNumber() + "\n");
                emailPresenter.append("From:" + m.getFrom()[0] + "\n");
                emailPresenter.append("Subject: " + m.getSubject() + "\n");
                emailPresenter.append("Text: " + readMessage(m) + "\n");
            }
        }catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}