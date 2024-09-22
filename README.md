# Rank_DBM_App


Rank_DBM_App is a rank database management application. It lets a user to log in with his administrator credentials into a PostgreSQL database (can be local or remote) and perform CRUD and other operations on the different entities of the rank database. A CLI has beeen implemented via java and operations on the database using a java API - jdbc. All the queries and other SQL statements are executed via prepared statements and for arguments bind parameters are used to prevent SQL injections. 

<br>

Rank database
---

The database has a "rank_sys" scehma and inside of it 4 entities: ranks, players, match_players, and matches. Players can enter matches and either win, lose or tie them. From this they can earn positive/negative elo points, which get added to their account via triggers (trigger.sql). With these elo points their rank gets detirmened and assigned. The more elo points they earn the bigger their rank is. Their match result is determined by the Matches table and its field winning_team. If the player during that match was a part of the winning_team he gets added elo points, if part of the losing he gets subtracted elo points and if it was a tie he gets added a smaller amount.

<br>

**The ER diagram of the database**

<img src="https://github.com/user-attachments/assets/cdcaeafc-3509-49d7-8876-6f6d06e7cdf0" width="800">

<br>


**The relational schema of the database**

![rankDbRelationalSchema](https://github.com/user-attachments/assets/000a75b0-0744-4dc2-b146-6053917796c1)


<br>


Entities of the database
---

All rows were gotten from running the insertion commands in the insertionsAndExamples.sql file.

<br>

**Players entity** - used to register players and store their elo points and rank (rank_id as the FK to Ranks entity)

![playerDbPicture](https://github.com/user-attachments/assets/e25e8cb0-a2ac-4f78-97fc-416247b8624d)

**Ranks entity** - used to store all the possible ranks a player can get, and those ranks elo interval (elomin - elomax), by which triggers and the application decide what rank what player gets.

![RankDbPicture](https://github.com/user-attachments/assets/41e98ab0-7ae7-4ec2-a759-4a7c2646492f)

**Matches entity** - used to record when and what match happened. Also stores which team of the match won. Its field winning_team can either have a 2, 1 or a 0 integer. The 1 and 2 symbolize the two teams which were playing the match and the 0 integer means there was't a winning team, therefore it's a tie.

![MatchesDbPicture](https://github.com/user-attachments/assets/6c7c6225-68fa-44b2-ba2a-158a1821f70c)

**Match_players entity** - used as a junction table between Players and Matches entities, to resolve the many-to-many relation between them. A player can participate in multiple matches and a match can have multiple players. Each mach is allowed up to 4 players, 2 players per team, and two teams. Therefore, triggers were used to ensure this integrity.

![matchPlayersPicture](https://github.com/user-attachments/assets/ef58a1af-c28f-44f5-8c2a-1a587ef715ef)

