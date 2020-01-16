-- Users with Posts
SELECT concat('userid',';','email',';', 'username',';', 'password',';','hash',';', 'name')
UNION
SELECT distinct concat(t1.user_id,';',user_email,';',username,';',concat(user_email,user_email), ';', user_password,';',coalesce(t2.name,t1.username))
FROM jforum_users t1
INNER JOIN jforum_posts t3
ON t1.user_id=t3.user_id
LEFT JOIN customer t2
ON t1.user_email=t2.email
