# Rank_DBM_App


Rank_DBM_App is a rank database management application. It lets a user to log in with his administrator credentials into a PostgreSQL database (can be local or remote) and perform CRUD and other operations on the different entities of the rank database. A CLI has beeen implemented via java and operations on the database using a java API - jdbc. All the queries and other SQL statements are executed via prepared statements and for arguments bind parameters are used to prevent SQL injections. 

<br>

Rank database
---

The database has a "rank_sys" scehma and inside of it 4 entities: ranks, players, match_players, and matches. Players can enter matches and either win, lose or tie them. From this they can earn positive/negative elo points, which get added to their account via triggers (trigger.sql). With these elo points their rank gets detirmened and assigned. The more elo points they earn the bigger their rank is.

<br>

**The ER diagram of the database**

![ER_rank_system](https://github.com/user-attachments/assets/547a154d-ba7a-4e23-8291-8764e264b1c2)

<br>

**The relational schema of the database**

![dbRelationalSchema](https://github.com/user-attachments/assets/c2c3e58b-b0a1-4231-9a02-327463d5bb80)

<br>

Tables
---


