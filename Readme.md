# DiscourseTransfer
Written in Java and uses Discourse API, has no dependencies on Discourse environment so can be run anywhere, used to import data 
from csv files into a Discourse installation. The only exception is if want to preserve user passwords then there is one database 
file to be imported directly into database.

In my case the data has come from JForum, but except for a few data processing quirks not coded specifically for JForum.
Because uses API running from different machine to server not suitable for importing really large forums (my usecase is just 
2,000 users and 12,000 posts, can be seen at https://community.jthink.net)

Quite basic, have only done importing of:

* users
* categories
* topics
* posts 

Amongst other things the following are not imported:

* Private messages
* Avatars (but if user has gravatar associated with email that will get picked up)
* Images
* Attachments

Expected this is of **most use to Java developers moving to Discourse from another forum**, code can be freely modified as required ( CreativeCommons Attribution-ShareAlike license)

There are many scripts already available as part of Discourse written in Ruby on Rails and for most users these will probably be more suitable.

Because we are using api only and had no dependencies on Discourse code you can run DiscourseTransfer from wherever you like
(i.e your own pc dev environment), just require standard dev tools familiar to most Java developers:
 
* java 
* git
* maven
 
## License
Licensed under CreativeCommons Attribution-ShareAlike license, please see https://creativecommons.org/licenses/by-sa/4.0/
 
## Use as follows:

#### Optional - Empty Database
May want to start again and clean out any existing data if have been experimenting

* If you have previously completed Wizards details properly might want to copy the details somewhere before deleting db  
* ssh discourse instance
* cd /var/discourse/shared/standalone
* rm -rf postgres*
* cd /var/discourse
* ./launcher rebuild app   
* Login to your discourse forum 
* Activate user
* Go through Install Wizard
* Edit the Announcement topic to remove the Discourse boilerplate
 
#### Setup ApiKey for all Users
In Admin setup *ApiKey* for *All Users*, and store it somewhere

#### Modify Discourse Config
Necessary if want to do either of:

* Allow users to login with old password (and old forum stored them as a oneway hash)
* Disable the api throttling 

* ssh discourse instance
* cd /var/discourse
* vi container/app.yml
* Add to env section to disable throttling
```
DISCOURSE_MAX_ADMIN_API_REQS_PER_KEY_PER_MINUTE: 6000
```
* Add to end section to allow old password
```
- git clone https://github.com/discoursehosting/discourse-migratepassword
```
* ./launcher rebuild app   

To complete the password config:

* Login to your Discourse forum    
* Select Admin/Plugs/migrated-password
* Select Settings
* Enable 'Allow migratepassword allow insecure passwords' (because existing user passwords probably to short for default Discourse requirements)   

#### Optional - Revise your Discourse Admin settings
We set the skip_validations flag when we create posts and topics so that if they do not meet all the rules the topics are still imported, 
if you want to abide by the rules you can switch the flag off (by modifying call to code)

e.g by default Discourse does not let creation of topics with title < 15, or body < 20 so if you have topics like this they will not get imported by default. 
Conversely it does not allow text bodies greater than 32,000.

You can modify all this by going to *Admin:Settings:Posting* and changing: 

- min post length
- min first post length
- min topic title length
- max post length

Discourse has minimum username length of 3 char, your old forum may support two characters, you can amend in *Admin:settings:users*

- min username length

Prevent emails for new users at *Admin:Settings:Email* 

- disable digest emails
- disable emails 

#### Extract Data from Old Forum

* Extract data from your old forum (see Forum Export section below)
* Scan for any illegal characters in the output, maybe due to incorrect encoding
 
#### Modify Special Users in User Import file

* If have anonymous user need to add an email address for them and edit the users.csv file
* If you have username with same name as the admin user running the script need to remove them from users.csv file

#### Import Data Into Discourse
DiscourseTransfer can be run as one process to import all data in one go. However my experience has pushed me towards doing one file at a time, checking
everything went well before progressing to the next file as a better approach, so we explain import one file at a time. 

#### Import Users
It is expected your Discourse database is (nearly) empty before import

Users are imported from the file specified by -uf. We output a mapping from old forum
user id to new Discourse user id to the file specified by -um option although this is not used by topic/post import as that is based on username rather than id.
If -p is specified we create an password file that can be imported into database to allow users to use their existing password 
 
```
DiscourseTransfer -u username -a apiKey -w website -uf users.csv -um users_map.csv -p importhash.csv
```

DiscourseTransfer validates your users file before doing actual import. If it finds any problems then it will exit before starting actual import, 
checks for the following issues:

* Discourse does not allow spaces, _,*, @,! in username
* Discourse requires valid looking email address for all users
* Discourse does not allow two different accounts to have same email address

If it finds any problems then you'll need to edit users.csv, and if change requires modifying the username you'll need to modify topics and posts
import files as well to update with new username.  As this can be difficult to do manually if you have more than handful of changes, we also output 
some sed code (that can be run on linux) to make it simpler to modify the csv files.

If it passes validation stage but DiscourseTransfer has a problem importing users then it will exit

#### Import Categories

Categories are imported from the file specified by -cf, and all are owned by the admin user specified in -u, we output a mapping from old forum
category id to new Discourse category id to the file specified by -cm option 
```
DiscourseTransfer -u username -a apiKey -w website -cf categories.csv -cm categories_map.csv 
```
If DiscourseTransfer has a problem importing users then it will exit

#### Import Topics

Topics are imported from the file specified by -tf, -cm file (from previous step) is required to allow the topics to be put into the 
correct category, we output a mapping from old forum topic id to new Discourse topic id to the file specified by -tm option 
```
DiscourseTransfer -u username -a apiKey -w website -tf topics.csv -tm topics_map.csv -cm categories_map.csv -tr topics_redirect_map.csv
```

If DiscourseTransfer fails to import any topics it will still continue to completion, this is because issues such as timeouts can make it difficult
to get a 100% successful import. 

#### Import Posts

Posts are imported from the file specified by -pf, -tm file (from previous step) is required to allow the posts to be put into the 
correct topic, we output a mapping from old forum post id to new Discourse post id to the file specified by -pm option 
```
DiscourseTransfer -u username -a apiKey -w website -pf posts.csv -pm posts_map.csv -tm topics_map.csv 
```

If DiscourseTransfer fails to import any posts it will still continue to completion, this is because issues such as timeouts can make it difficult
to get a 100% successful import. 

#### Failed Import
If import fails you'll want to fix the issue then possibly clear out all the data before trying again
  
Quickest way maybe:
```
delete from users where id > 1;
delete from user_emails where user_id >1;
delete from posts;
delete from topics;
delete from categories;
```

#### Optional - Import Password Hash File
Transfer the importhash.csv file 

e.g ftp to dev server
```
ssh discourse instance
cd /var/discourse/shared/standalone/uploads (this folder is visible within container)
ftp devserver
cd discoursetransfer/src/main/resources/importhash.csv
ftp get importhash.csv
```

(or git clone discoursetransfer and commit your version of importhash.csv)

Import into database
e.g
```
./launcher enter app   
su postgres -c "psql discourse"
\copy user_custom_fields(user_id,name, value, created_at, updated_at) FROM '/shared/uploads/importhash.csv' DELIMITER ';' CSV;
```

#### Optional - Post Import Clean Up

* Import just imports one level of category, in JForum I have Categories and Forums, decided it was easy enough to manually create
two levels of categories with Discourse itself after import
* Remove All Users Api Key
* Modify app.yaml file to remove relsxed api throttling and rebuild
* Edit the autocreated first topics created for each category
* Reenable disable emails option 

#### Optional - Longer term Post Import Clean Up
After giving existing users a period of time to login modify app.yaml file to remove importpassword and rebuild

## Forum Export
Creating the csv files is up to you, how it is done depends on your forum but should be quite easy, we have examples
for jforum further below.

###User Csv Files
Format of users.csv should contain header line and then fields names e.g:
```
userid;email;username
UserId1;Email;UserName
UserId2;Email1;UserName1 
```

Above is minimum required, but you can use additional fields defined in net.jthink.discoursetransfer.csv.UserCsv

e.g this is also valid

```
userid;email;username;hash;password;name
UserId1;Email1;UserName1;Password1;Name1 
UserId1;Email2;UserName2;Password2;Name2 
```

### JForum Export
#### Users
Name is not stored in jforum so we use username again

```
mysql jforum < src/main/resources/jforum/users.sql > users.csv
```
#### Categories
```
mysql jforum < src/main/resources/jforum/categories.sql > categories.csv
```
#### Topics
```
mysql jforum < src/main/resources/jforum/topics.sql > topics.csv
```
#### Posts
```
mysql jforum < src/main/resources/jforum/posts.sql > posts.csv
```
