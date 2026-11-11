package ui;

import dao.UserDAO;
import utils.Session;
import utils.UIConstants;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final JTextField username = new JTextField(18);
    private final JPasswordField password = new JPasswordField(18);

    public LoginFrame() {
        setTitle("FINORA | Smart Finance Management");
        setSize(440,330); setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(UIConstants.BACKGROUND);
        JPanel panel=new JPanel(new GridBagLayout()); panel.setBackground(UIConstants.BACKGROUND);
        GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(8,8,8,8);g.fill=GridBagConstraints.HORIZONTAL;
        JLabel title=new JLabel("FINORA",SwingConstants.CENTER);title.setFont(new Font("Segoe UI",Font.BOLD,30));title.setForeground(UIConstants.DARK_GREEN);
        g.gridx=0;g.gridy=0;g.gridwidth=2;panel.add(title,g);
        g.gridwidth=1;g.gridy++;panel.add(new JLabel("Username"),g);g.gridx=1;panel.add(username,g);
        g.gridx=0;g.gridy++;panel.add(new JLabel("Password"),g);g.gridx=1;panel.add(password,g);
        JButton login=new JButton("Login"), register=new JButton("Create account");
        g.gridx=0;g.gridy++;panel.add(login,g);g.gridx=1;panel.add(register,g);
        login.addActionListener(e->doLogin());
        register.addActionListener(e->new RegisterDialog(this).setVisible(true));
        add(panel);
    }
    private void doLogin() {
        try {
            int id=new UserDAO().login(username.getText().trim(),new String(password.getPassword()));
            if(id==0){JOptionPane.showMessageDialog(this,"Incorrect username or password.");return;}
            String name=new UserDAO().getFullName(id);
            Session.start(id,username.getText().trim(),name);
            dispose();new MainFrame().setVisible(true);
        } catch(Exception ex){JOptionPane.showMessageDialog(this,"Login error: "+ex.getMessage());}
    }
}
