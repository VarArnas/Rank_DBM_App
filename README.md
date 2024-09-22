# Rank_DBM_App


Rank_DBM_App is a rank database management application. It lets a user to log in with his administrator credentials into a PostgreSQL database (can be local or remote) and perform CRUD and other operations on the different entities of the rank database. A CLI has beeen implemented via ***Java*** and operations on the database using a java API - ***jdbc***. All the queries and other SQL statements are executed via prepared statements and for arguments bind parameters are used to prevent SQL injections. 

<br>

Rank database
---

The database has a **rank_sys** scehma and inside of it 4 entities: *Ranks*, *Players*, *Match_players*, and *Matches*. *Players* can enter matches and either win, lose or tie them. From this they can earn positive/negative elo points, which get added to their account via triggers (***trigger.sql***). With these elo points their rank gets detirmened and assigned. The more elo points they earn the bigger their rank is. Their match result is determined by the *Matches* table and its field *winning_team*. If the player during that match was a part of the *winning_team* he gets added elo points, if part of the losing he gets subtracted elo points and if it was a tie he gets added a smaller amount.

<br>

- **The ER diagram of the database**

<img src="https://github.com/user-attachments/assets/cdcaeafc-3509-49d7-8876-6f6d06e7cdf0" width="800">

<br>
<br>
<br>

- **The relational schema of the database**

![rankDbRelationalSchema](https://github.com/user-attachments/assets/000a75b0-0744-4dc2-b146-6053917796c1)


<br>


Entities of the database
---
> [!NOTE]
> All rows were gotten from running the insertion commands in the insertionsAndExamples.sql file.

<br>

- **Players entity** - used to register players and store their elo points and rank (*rank_id* as the FK to *Ranks* entity). Players can get a rank only after participating in more than 5 matches (ensured by triggers), therefore, some players do not have a rank.

![playerDbPicture](https://github.com/user-attachments/assets/e25e8cb0-a2ac-4f78-97fc-416247b8624d)

<br>

- **Ranks entity** - used to store all the possible ranks a player can get, and those ranks' elo interval (elomin - elomax), by which triggers and the application decide what rank what player gets.

![RankDbPicture](https://github.com/user-attachments/assets/41e98ab0-7ae7-4ec2-a759-4a7c2646492f)

<br>

- **Matches entity** - used to record when and what match happened. Also stores which team of the match won. Its field *winning_team* can either have a 2, 1 or a 0 integer. The 1 and 2 symbolize the two teams which were playing the match and the 0 integer means there was't a winning team, therefore it symbolizes a tie.

![MatchesDbPicture](https://github.com/user-attachments/assets/6c7c6225-68fa-44b2-ba2a-158a1821f70c)

<br>

- **Match_players entity** - used as a junction table between *Players* and *Matches* entities, to resolve the many-to-many relation between them. A player can participate in multiple matches and a match can have multiple players. Each mach is allowed up to 4 players, 2 players per team, and two teams. Therefore, triggers were used to ensure this integrity.

![matchPlayersPicture](https://github.com/user-attachments/assets/ef58a1af-c28f-44f5-8c2a-1a587ef715ef)

<br>

Views of the database
---

- **Players_with_ranks** - this view joins the *Players* and *Ranks* tables to display what rank (and it's name) each player has.

![ViewTablePicture](https://github.com/user-attachments/assets/6f46ca43-20bf-4908-90be-c86a21207a9b)

<br>

- **Player_amount_for_rank** - this materialzied view joins the *PLayers* and *Ranks* table, and counts how many players each rank has. To refresh this view use commnad `REFRESH MATERIALIZED VIEW player_amount_for_rank;` in psql.

![matViewPicture](https://github.com/user-attachments/assets/1ae6e21f-c4f0-4d36-a791-e0b28defdd88)

<br>

Rules for data integrity of the database (triggers)
---
> [!NOTE]
> Even though triggers do make the database a lot more inefficient, they were used to make the actual application client for managing the database as thin as possible. For more information on each trigger and to better understand these rules refer to ***triggers.sql*** file.

- **Elo points for players after matches** - after each match a player must get either +300/50/25 or -300/50. He gets +/- 300 if he hadn't played more than 5 matches (i.e. his match_amount field is < 5). If he had played more than 5 matches then he gets only +/- 50. If its a tie then the player get +25. The trigger which implements this is: ***after_match_players_insert***.

- **If a match gets updated or deleted the participants' of the match elo should also get updated** - if a match gets deleted or its field *winning_team* gets changed then all of the 4 participants of the match elo should also get updated to reflect that. This is impleneted through the applications business logic, via methods: ***ChangeMatchWinnerAndUpdateElo()*** and ***DeleteMatchAndUpdatePlayer()***, of the *MatchManager* class.

- **All rank intervals should be coherent** - inside the *Ranks* table each rank's elo interval (*elomin* - *elomax*), should have neighbouring rank intervals. One of the neighbour's interval's *elomax* should be equal to the rank's *elomin* and another neighbour's *elomin* should be equal to the rank's *elomax* (besides the lowest and highest ranks). Whenever a new rank gets inserted or deleted its neighbour ranks' intervals should get adjusted accordingly. This was implemented via the ***after_rank_deletion*** and ***before_rank_insertion triggers***.

- **The players' elo should be apart of their assigned rank's elo interval** - whenever a rank gets inserted/deleted and its neighbours' elo intervals get changed, each player's, if their *rank_id* was one of the neighbours, should be evaluated and changed, if now the elo interval inside of which was the player's elo is assigned to a different rank. The trigger which enforces this is: ***after_rank_insertion***.

- **4 players, 2 teams can participate in a match** - besides the 4 players, 2 teams per match, the same player is not allowed to pariticipate more than once in the same match. This was ensured via ***before_insert_match_players*** trigger.


 Application CLI
 ---

 ![CLIpicture](https://github.com/user-attachments/assets/0c5897e7-8d53-40a9-8233-81d79e03fcd8)


