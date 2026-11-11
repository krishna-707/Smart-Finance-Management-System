package dao;

import database.DBConnection;
import utils.PasswordUtil;
import java.sql.*;

public class UserDAO {
    public boolean register(String username, String fullName, String email, String password) throws Exception {
        String salt = PasswordUtil.newSalt();
        String hash = PasswordUtil.hash(password, salt);
        String sql = "INSERT INTO users(username,full_name,email,password_hash,password_salt) VALUES(?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, username); p.setString(2, fullName); p.setString(3, email);
            p.setString(4, hash); p.setString(5, salt);
            p.executeUpdate();
            int id;
            try (ResultSet rs = p.getGeneratedKeys()) { rs.next(); id = rs.getInt(1); }
            try (PreparedStatement a = c.prepareStatement(
                    "INSERT INTO accounts(user_id,account_name,account_type,balance) VALUES(?,?,?,0)")) {
                a.setInt(1,id); a.setString(2,"Main Wallet"); a.setString(3,"General"); a.executeUpdate();
            }
            addCategory(c,id,"Salary","INCOME");
            addCategory(c,id,"Other Income","INCOME");
            addCategory(c,id,"Food","EXPENSE");
            addCategory(c,id,"Transport","EXPENSE");
            addCategory(c,id,"Shopping","EXPENSE");
            addCategory(c,id,"Bills","EXPENSE");
            addCategory(c,id,"Other Expense","EXPENSE");
            try (PreparedStatement h = c.prepareStatement(
                    "INSERT INTO financial_health(user_id,score,stage,points) VALUES(?,0,'Seed',0)")) {
                h.setInt(1,id); h.executeUpdate();
            }
            return true;
        }
    }
    private void addCategory(Connection c,int id,String name,String type) throws SQLException {
        try (PreparedStatement p=c.prepareStatement(
                "INSERT INTO categories(user_id,category_name,category_type) VALUES(?,?,?)")) {
            p.setInt(1,id); p.setString(2,name); p.setString(3,type); p.executeUpdate();
        }
    }
    public int login(String username, String password) throws Exception {
        try (Connection c=DBConnection.getConnection();
             PreparedStatement p=c.prepareStatement("SELECT user_id,password_hash,password_salt FROM users WHERE username=?")) {
            p.setString(1,username);
            try(ResultSet r=p.executeQuery()) {
                if(!r.next()) return 0;
                return PasswordUtil.verify(password,r.getString("password_salt"),r.getString("password_hash"))
                    ? r.getInt("user_id") : 0;
            }
        }
    }
    public String getFullName(int id) throws SQLException {
        try(Connection c=DBConnection.getConnection();
            PreparedStatement p=c.prepareStatement("SELECT full_name FROM users WHERE user_id=?")) {
            p.setInt(1,id);
            try(ResultSet r=p.executeQuery()) { return r.next()?r.getString(1):"User"; }
        }
    }
}
