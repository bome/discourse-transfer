SELECT concat(
'topicid',';',
'forumid',';',
'username',';',
'topic_title',';',
'post_time',';',
'post_text',";",
'topic_views'
)
UNION
SELECT
 concat(
 t.topic_id,';'
 ,t.forum_id,';'
 ,u.username,';'
  ,replace(replace(replace(replace(topic_title,';','[SEMICOLON]'),'\n','[NEWLINE]'),"\r",""),'"','[DOUBLEQUOTE]'),';'
 ,t.topic_time,';'
 ,replace(replace(replace(replace(p.post_text,';','[SEMICOLON]'),'\n','[NEWLINE]'),"\r",""),'"','[DOUBLEQUOTE]'),";"
 ,t.topic_views
 )
FROM jforum_topics t
JOIN jforum_posts_text p
ON t.topic_first_post_id=p.post_id
JOIN jforum_users u
ON t.user_id=u.user_id
;