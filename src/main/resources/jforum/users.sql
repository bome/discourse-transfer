-- All Users
SELECT concat('userid',';','email',';', 'username',';', 'password',';', 'name')
UNION
SELECT concat(user_id,';',user_email,';',username,';',user_password,';',username)
FROM jforum_users;
