-- FINORA demo data for the account with username 'demo'
-- Safe to run again: it replaces demo records in finance modules only.
-- It does NOT change the demo user's password, profile, categories, or account.

USE finora_db;

SET @uid := (SELECT user_id FROM users WHERE username = 'demo' LIMIT 1);

-- Stop if the demo account has not been registered yet.
DELIMITER $$
DROP PROCEDURE IF EXISTS finora_check_demo_user$$
CREATE PROCEDURE finora_check_demo_user()
BEGIN
    IF @uid IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Demo user not found. Register and log in with username demo first.';
    END IF;
END$$
DELIMITER ;
CALL finora_check_demo_user();
DROP PROCEDURE finora_check_demo_user;

SET @aid := (SELECT account_id FROM accounts WHERE user_id=@uid ORDER BY account_id LIMIT 1);
SET @income_salary := (SELECT category_id FROM categories WHERE user_id=@uid AND category_name='Salary' AND category_type='INCOME' LIMIT 1);
SET @income_other := (SELECT category_id FROM categories WHERE user_id=@uid AND category_name='Other Income' AND category_type='INCOME' LIMIT 1);
SET @food := (SELECT category_id FROM categories WHERE user_id=@uid AND category_name='Food' AND category_type='EXPENSE' LIMIT 1);
SET @transport := (SELECT category_id FROM categories WHERE user_id=@uid AND category_name='Transport' AND category_type='EXPENSE' LIMIT 1);
SET @shopping := (SELECT category_id FROM categories WHERE user_id=@uid AND category_name='Shopping' AND category_type='EXPENSE' LIMIT 1);
SET @bills := (SELECT category_id FROM categories WHERE user_id=@uid AND category_name='Bills' AND category_type='EXPENSE' LIMIT 1);
SET @other_expense := (SELECT category_id FROM categories WHERE user_id=@uid AND category_name='Other Expense' AND category_type='EXPENSE' LIMIT 1);

-- Clear only this demo user's previous sample records, allowing safe re-runs.
DELETE FROM transactions WHERE user_id=@uid;
DELETE FROM budgets WHERE user_id=@uid;
DELETE FROM savings_goals WHERE user_id=@uid;
DELETE FROM subscriptions WHERE user_id=@uid;
DELETE FROM wishlist_items WHERE user_id=@uid;
DELETE FROM salary_schedules WHERE user_id=@uid;
UPDATE accounts SET balance=0 WHERE user_id=@uid;

-- Sample transactions: total income Rs. 65,000; total expenses Rs. 28,750.
INSERT INTO transactions (user_id,account_id,category_id,amount,transaction_type,description,transaction_date) VALUES
(@uid,@aid,@income_salary,60000.00,'INCOME','Monthly salary',DATE_SUB(CURDATE(),INTERVAL 10 DAY)),
(@uid,@aid,@income_other,5000.00,'INCOME','Freelance project payment',DATE_SUB(CURDATE(),INTERVAL 8 DAY)),
(@uid,@aid,@food,850.00,'EXPENSE','Groceries and essentials',DATE_SUB(CURDATE(),INTERVAL 7 DAY)),
(@uid,@aid,@transport,450.00,'EXPENSE','Fuel and commute',DATE_SUB(CURDATE(),INTERVAL 6 DAY)),
(@uid,@aid,@bills,2200.00,'EXPENSE','Electricity and internet bills',DATE_SUB(CURDATE(),INTERVAL 5 DAY)),
(@uid,@aid,@food,620.00,'EXPENSE','Restaurant and snacks',DATE_SUB(CURDATE(),INTERVAL 4 DAY)),
(@uid,@aid,@shopping,3499.00,'EXPENSE','Clothing purchase',DATE_SUB(CURDATE(),INTERVAL 3 DAY)),
(@uid,@aid,@transport,300.00,'EXPENSE','Bus and cab rides',DATE_SUB(CURDATE(),INTERVAL 2 DAY)),
(@uid,@aid,@other_expense,1800.00,'EXPENSE','Medical and miscellaneous',DATE_SUB(CURDATE(),INTERVAL 2 DAY)),
(@uid,@aid,@food,731.00,'EXPENSE','Weekly groceries',DATE_SUB(CURDATE(),INTERVAL 1 DAY)),
(@uid,@aid,@bills,18300.00,'EXPENSE','Rent and household contribution',CURDATE());

-- Keep the wallet balance consistent with the sample transactions.
UPDATE accounts SET balance=36250.00 WHERE account_id=@aid;

-- Monthly budgets for the current month.
INSERT INTO budgets (user_id,category_id,budget_month,budget_year,limit_amount) VALUES
(@uid,@food,MONTH(CURDATE()),YEAR(CURDATE()),8000.00),
(@uid,@transport,MONTH(CURDATE()),YEAR(CURDATE()),4000.00),
(@uid,@shopping,MONTH(CURDATE()),YEAR(CURDATE()),6000.00),
(@uid,@bills,MONTH(CURDATE()),YEAR(CURDATE()),22000.00),
(@uid,@other_expense,MONTH(CURDATE()),YEAR(CURDATE()),3000.00);

-- Savings goals.
INSERT INTO savings_goals (user_id,goal_name,target_amount,saved_amount,target_date) VALUES
(@uid,'Emergency Fund',100000.00,35000.00,DATE_ADD(CURDATE(),INTERVAL 180 DAY)),
(@uid,'New Laptop',90000.00,42000.00,DATE_ADD(CURDATE(),INTERVAL 120 DAY)),
(@uid,'Vacation Fund',30000.00,8500.00,DATE_ADD(CURDATE(),INTERVAL 240 DAY));

-- Active subscriptions.
INSERT INTO subscriptions (user_id,subscription_name,amount,billing_cycle,renewal_date,active) VALUES
(@uid,'Netflix',649.00,'Monthly',DATE_ADD(CURDATE(),INTERVAL 5 DAY),TRUE),
(@uid,'Spotify Premium',119.00,'Monthly',DATE_ADD(CURDATE(),INTERVAL 12 DAY),TRUE),
(@uid,'Cloud Storage',1999.00,'Yearly',DATE_ADD(CURDATE(),INTERVAL 45 DAY),TRUE);

-- Buy Later entries. Their 24-hour waiting period has already elapsed for demo purposes.
INSERT INTO wishlist_items (user_id,item_name,price,added_at,decision_available_at,status) VALUES
(@uid,'Wireless Headphones',2999.00,DATE_SUB(NOW(),INTERVAL 3 DAY),DATE_SUB(NOW(),INTERVAL 2 DAY),'WAITING'),
(@uid,'Mechanical Keyboard',4499.00,DATE_SUB(NOW(),INTERVAL 2 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),'WAITING'),
(@uid,'Fitness Watch',5999.00,DATE_SUB(NOW(),INTERVAL 1 DAY),NOW(),'WAITING');

-- Upcoming expected income; not marked as received.
INSERT INTO salary_schedules (user_id,salary_name,amount,expected_date,confirmed) VALUES
(@uid,'Monthly Salary',60000.00,DATE_ADD(CURDATE(),INTERVAL 20 DAY),FALSE),
(@uid,'Freelance Project',7500.00,DATE_ADD(CURDATE(),INTERVAL 12 DAY),FALSE);

-- Illustrative demo health score/stage.
UPDATE financial_health SET score=78,stage='Growing',points=780 WHERE user_id=@uid;

-- Verification summary.
SELECT 'Demo account' AS item, username AS details FROM users WHERE user_id=@uid;
SELECT 'Transactions' AS item, COUNT(*) AS records FROM transactions WHERE user_id=@uid
UNION ALL SELECT 'Budgets', COUNT(*) FROM budgets WHERE user_id=@uid
UNION ALL SELECT 'Savings goals', COUNT(*) FROM savings_goals WHERE user_id=@uid
UNION ALL SELECT 'Subscriptions', COUNT(*) FROM subscriptions WHERE user_id=@uid
UNION ALL SELECT 'Wishlist items', COUNT(*) FROM wishlist_items WHERE user_id=@uid
UNION ALL SELECT 'Salary schedules', COUNT(*) FROM salary_schedules WHERE user_id=@uid;
SELECT account_name,balance FROM accounts WHERE user_id=@uid;
