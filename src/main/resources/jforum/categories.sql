SELECT concat('id',';', 'name',';', 'description',';', 'timestamp')
UNION
SELECT concat(f.forum_id,';', f.forum_name,';', f.forum_desc,';', x.first_post_time)
FROM jforum_forums f
LEFT OUTER JOIN
(
    SELECT MIN(topic_time) AS first_post_time, forum_id
    FROM jforum_topics
    GROUP BY forum_id
) x
ON f.forum_id = x.forum_id
;