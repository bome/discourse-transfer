SELECT concat(
'postid',';',
'topicid',';',
'forumid',';',
'username',';',
'post_time',';',
'post_text'
)
UNION
SELECT
 concat(
 p.post_id,';'
 ,p.topic_id,';'
 ,t.forum_id,';'
 ,u.username,';'
 ,p.post_time,';'
 ,replace(replace(replace(replace(pt.post_text,';','[SEMICOLON]'),'\n','[NEWLINE]'),"\r",""),'"','[DOUBLEQUOTE]')
 )
FROM jforum_posts  p
JOIN jforum_topics t
ON   p.topic_id=t.topic_id
AND  p.post_id!=t.topic_first_post_id
JOIN jforum_posts_text pt
ON   p.post_id=pt.post_id
JOIN jforum_users u
ON p.user_id=u.user_id
;