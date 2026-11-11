package dao;

import database.DBConnection;
import utils.Session;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class FinanceDAO {
    private int uid() { return Session.getUserId(); }

    public List<String[]> rows(String sql, Object... values) throws SQLException {
        List<String[]> result = new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            int i=1; for(Object v:values) p.setObject(i++,v);
            try(ResultSet r=p.executeQuery()) {
                int n=r.getMetaData().getColumnCount();
                while(r.next()) { String[] row=new String[n]; for(int j=1;j<=n;j++) row[j-1]=r.getString(j); result.add(row); }
            }
        }
        return result;
    }

    public void addAccount(String name,String type) throws SQLException {
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(
            "INSERT INTO accounts(user_id,account_name,account_type,balance) VALUES(?,?,?,0)")) {
            p.setInt(1,uid()); p.setString(2,name); p.setString(3,type); p.executeUpdate();
        }
    }

    public List<String[]> accounts() throws SQLException {
        return rows("SELECT account_id,account_name,account_type,balance FROM accounts WHERE user_id=? ORDER BY account_id",uid());
    }
    public List<String[]> categories(String type) throws SQLException {
        return rows("SELECT category_id,category_name FROM categories WHERE user_id=? AND category_type=? ORDER BY category_name",uid(),type);
    }

    public void addTransaction(int accountId,int categoryId,BigDecimal amount,String type,String description,LocalDate date) throws SQLException {
        String sql="INSERT INTO transactions(user_id,account_id,category_id,amount,transaction_type,description,transaction_date) VALUES(?,?,?,?,?,?,?)";
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try(PreparedStatement p=c.prepareStatement(sql)) {
                p.setInt(1,uid()); p.setInt(2,accountId); p.setInt(3,categoryId); p.setBigDecimal(4,amount);
                p.setString(5,type); p.setString(6,description); p.setDate(7,java.sql.Date.valueOf(date)); p.executeUpdate();
            }
            String sign=type.equals("INCOME")?"+":"-";
            try(PreparedStatement p=c.prepareStatement("UPDATE accounts SET balance=balance "+sign+" ? WHERE account_id=? AND user_id=?")) {
                p.setBigDecimal(1,amount); p.setInt(2,accountId); p.setInt(3,uid()); p.executeUpdate();
            }
            c.commit();
        }
    }

    public void addBudget(int categoryId,int month,int year,BigDecimal amount) throws SQLException {
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(
          "INSERT INTO budgets(user_id,category_id,budget_month,budget_year,limit_amount) VALUES(?,?,?,?,?) " +
          "ON DUPLICATE KEY UPDATE limit_amount=VALUES(limit_amount)")) {
            p.setInt(1,uid());p.setInt(2,categoryId);p.setInt(3,month);p.setInt(4,year);p.setBigDecimal(5,amount);p.executeUpdate();
        }
    }
    public void addGoal(String name,BigDecimal target,BigDecimal saved,LocalDate date) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(
          "INSERT INTO savings_goals(user_id,goal_name,target_amount,saved_amount,target_date) VALUES(?,?,?,?,?)")) {
            p.setInt(1,uid());p.setString(2,name);p.setBigDecimal(3,target);p.setBigDecimal(4,saved);
            p.setDate(5,date==null?null:java.sql.Date.valueOf(date));p.executeUpdate();
        }
    }
    public void updateGoal(int goalId,String name,BigDecimal target,LocalDate date) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(
          "UPDATE savings_goals SET goal_name=?,target_amount=?,target_date=? WHERE goal_id=? AND user_id=?")) {
            p.setString(1,name);p.setBigDecimal(2,target);
            p.setDate(3,date==null?null:java.sql.Date.valueOf(date));
            p.setInt(4,goalId);p.setInt(5,uid());
            if(p.executeUpdate()==0) throw new SQLException("Savings goal not found.");
        }
    }
    public void addGoalMoney(int goalId,BigDecimal amount) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(
          "UPDATE savings_goals SET saved_amount=saved_amount+? WHERE goal_id=? AND user_id=?")) {
            p.setBigDecimal(1,amount);p.setInt(2,goalId);p.setInt(3,uid());
            if(p.executeUpdate()==0) throw new SQLException("Savings goal not found.");
        }
    }
    public void deleteGoal(int goalId) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(
          "DELETE FROM savings_goals WHERE goal_id=? AND user_id=?")) {
            p.setInt(1,goalId);p.setInt(2,uid());
            if(p.executeUpdate()==0) throw new SQLException("Savings goal not found.");
        }
    }
    public void addSubscription(String name,BigDecimal amount,String cycle,LocalDate date) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(
          "INSERT INTO subscriptions(user_id,subscription_name,amount,billing_cycle,renewal_date) VALUES(?,?,?,?,?)")) {
            p.setInt(1,uid());p.setString(2,name);p.setBigDecimal(3,amount);p.setString(4,cycle);p.setDate(5,java.sql.Date.valueOf(date));p.executeUpdate();
        }
    }
    public void addWishlist(String name,BigDecimal price) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(
          "INSERT INTO wishlist_items(user_id,item_name,price,decision_available_at) VALUES(?,?,?,DATE_ADD(NOW(),INTERVAL 24 HOUR))")) {
            p.setInt(1,uid());p.setString(2,name);p.setBigDecimal(3,price);p.executeUpdate();
        }
    }
    public void updateWishlist(int id,String status) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(
          "UPDATE wishlist_items SET status=? WHERE wishlist_id=? AND user_id=? AND decision_available_at<=NOW()")) {
            p.setString(1,status);p.setInt(2,id);p.setInt(3,uid());
            if(p.executeUpdate()==0) throw new SQLException("The 24-hour waiting period is not over yet.");
        }
    }
    public void addSalary(String name,BigDecimal amount,LocalDate date) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(
          "INSERT INTO salary_schedules(user_id,salary_name,amount,expected_date) VALUES(?,?,?,?)")) {
            p.setInt(1,uid());p.setString(2,name);p.setBigDecimal(3,amount);p.setDate(4,java.sql.Date.valueOf(date));p.executeUpdate();
        }
    }
    public void confirmSalary(int id) throws SQLException {
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            BigDecimal amount=null;
            try(PreparedStatement p=c.prepareStatement("SELECT amount FROM salary_schedules WHERE salary_id=? AND user_id=? AND confirmed=FALSE")) {
                p.setInt(1,id);p.setInt(2,uid());try(ResultSet r=p.executeQuery()){if(r.next())amount=r.getBigDecimal(1);}
            }
            if(amount==null) throw new SQLException("Salary not found or already confirmed.");
            int accountId;
            try(PreparedStatement p=c.prepareStatement("SELECT account_id FROM accounts WHERE user_id=? ORDER BY account_id LIMIT 1")) {
                p.setInt(1,uid());try(ResultSet r=p.executeQuery()){r.next();accountId=r.getInt(1);}
            }
            int categoryId;
            try(PreparedStatement p=c.prepareStatement("SELECT category_id FROM categories WHERE user_id=? AND category_type='INCOME' ORDER BY category_id LIMIT 1")) {
                p.setInt(1,uid());try(ResultSet r=p.executeQuery()){r.next();categoryId=r.getInt(1);}
            }
            try(PreparedStatement p=c.prepareStatement("UPDATE salary_schedules SET confirmed=TRUE WHERE salary_id=?")){p.setInt(1,id);p.executeUpdate();}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO transactions(user_id,account_id,category_id,amount,transaction_type,description,transaction_date) VALUES(?,?,?,?,'INCOME','Confirmed salary',CURDATE())")) {
                p.setInt(1,uid());p.setInt(2,accountId);p.setInt(3,categoryId);p.setBigDecimal(4,amount);p.executeUpdate();
            }
            try(PreparedStatement p=c.prepareStatement("UPDATE accounts SET balance=balance+? WHERE account_id=?")){p.setBigDecimal(1,amount);p.setInt(2,accountId);p.executeUpdate();}
            c.commit();
        }
    }

    public String dashboard() throws SQLException {
        List<String[]> a=rows("SELECT COALESCE(SUM(balance),0) FROM accounts WHERE user_id=?",uid());
        List<String[]> i=rows("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE user_id=? AND transaction_type='INCOME' AND MONTH(transaction_date)=MONTH(CURDATE()) AND YEAR(transaction_date)=YEAR(CURDATE())",uid());
        List<String[]> e=rows("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE user_id=? AND transaction_type='EXPENSE' AND MONTH(transaction_date)=MONTH(CURDATE()) AND YEAR(transaction_date)=YEAR(CURDATE())",uid());
        return "Total balance: ₹"+a.get(0)[0]+"     |     This month's income: ₹"+i.get(0)[0]+"     |     This month's expenses: ₹"+e.get(0)[0];
    }
    public List<String[]> transactions() throws SQLException {
        return rows("SELECT t.transaction_id,a.account_name,c.category_name,t.transaction_type,t.amount,t.description,t.transaction_date FROM transactions t JOIN accounts a ON t.account_id=a.account_id JOIN categories c ON t.category_id=c.category_id WHERE t.user_id=? ORDER BY t.transaction_date DESC,t.transaction_id DESC",uid());
    }
    public List<String[]> budgets() throws SQLException {
        return rows("SELECT b.budget_id,c.category_name,b.budget_month,b.budget_year,b.limit_amount,COALESCE(SUM(t.amount),0) AS spent FROM budgets b JOIN categories c ON b.category_id=c.category_id LEFT JOIN transactions t ON t.category_id=b.category_id AND t.transaction_type='EXPENSE' AND MONTH(t.transaction_date)=b.budget_month AND YEAR(t.transaction_date)=b.budget_year WHERE b.user_id=? GROUP BY b.budget_id,c.category_name,b.budget_month,b.budget_year,b.limit_amount ORDER BY b.budget_year DESC,b.budget_month DESC",uid());
    }
    public List<String[]> goals() throws SQLException { return rows("SELECT goal_id,goal_name,target_amount,saved_amount,target_date FROM savings_goals WHERE user_id=? ORDER BY goal_id DESC",uid()); }
    public List<String[]> subscriptions() throws SQLException { return rows("SELECT subscription_id,subscription_name,amount,billing_cycle,renewal_date,active FROM subscriptions WHERE user_id=? ORDER BY renewal_date",uid()); }
    public List<String[]> wishlist() throws SQLException { return rows("SELECT wishlist_id,item_name,price,added_at,decision_available_at,status FROM wishlist_items WHERE user_id=? ORDER BY wishlist_id DESC",uid()); }
    public List<String[]> salaries() throws SQLException { return rows("SELECT salary_id,salary_name,amount,expected_date,confirmed FROM salary_schedules WHERE user_id=? ORDER BY expected_date DESC",uid()); }
    public List<String[]> report(int month,int year) throws SQLException {
        return rows("SELECT transaction_type,COALESCE(SUM(amount),0) FROM transactions WHERE user_id=? AND MONTH(transaction_date)=? AND YEAR(transaction_date)=? GROUP BY transaction_type",uid(),month,year);
    }
    public List<String[]> health() throws SQLException {
        return rows("SELECT score,stage,points FROM financial_health WHERE user_id=?",uid());
    }
}
