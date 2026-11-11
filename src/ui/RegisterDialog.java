package ui;

import dao.UserDAO;
import javax.swing.*;
import java.awt.*;

public class RegisterDialog extends JDialog {
    public RegisterDialog(JFrame parent) {
        super(parent,"Create FINORA account",true);
        JTextField username=new JTextField(18), name=new JTextField(18), email=new JTextField(18);
        JPasswordField pass=new JPasswordField(18);
        JPanel p=new JPanel(new GridLayout(0,2,8,8));
        p.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        p.add(new JLabel("Full name"));p.add(name);
        p.add(new JLabel("Username"));p.add(username);
        p.add(new JLabel("Email"));p.add(email);
        p.add(new JLabel("Password"));p.add(pass);
        JButton create=new JButton("Register");p.add(new JLabel());p.add(create);
        create.addActionListener(e->{
            try {
                if(name.getText().isBlank()||username.getText().isBlank()||email.getText().isBlank()||pass.getPassword().length<6){
                    JOptionPane.showMessageDialog(this,"Fill all fields. Password must have at least 6 characters.");return;
                }
                new UserDAO().register(username.getText().trim(),name.getText().trim(),email.getText().trim(),new String(pass.getPassword()));
                JOptionPane.showMessageDialog(this,"Account created. You can now log in.");dispose();
            } catch(Exception ex){JOptionPane.showMessageDialog(this,"Registration failed: "+ex.getMessage());}
        });
        add(p);pack();setLocationRelativeTo(parent);
    }
}
