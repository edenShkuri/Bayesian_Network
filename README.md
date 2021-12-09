# Bayesian_Network

## Data Structures:
### Bn_Node:
represent a node in the graph / event in the Bayesia network.
each node has a name(string), a list of outcomes (for example T or F),and a CPT(conditional probability table).

### Bayesian_Net:  
the graph data structuer that represent the network.

### Factor:
hold the factor name and the factor table (for the variable_elimination).

## Algorithms:
### Bayes_ball -
Given a query - returns "yes" if the variables are independent or "no" not.
the purpose of the algorithm is to fine if a couple of events are independent or not, given (or not) the values of other events.
if ther is a path from one of the events (which are represented by vertices in the graph) to the other one - they are **not independent**, if there isn't - thery are **independent**.
the algorithm is similar to the regular BFS exept the part that instead of going to every neighbor of each vertex there are some rules that determine who we can go to:
- if we came to a vertex throw his **parent** and he is **given** in the query then we can go from this vertex only to **his parents**.
- if we came to a vertex throw his **parent** and he is **not given** in the query then we can go from this vertex only to **his children**.
- if we came to a vertex throw his **child** and he is **given** in the query then we **can't go any other vertices**.
- if we came to a vertex throw his **child** and he is **not given** in the query then we can go from this vertex to **every other vertex**.

<img width="200" alt="bayes_ball" src="https://user-images.githubusercontent.com/74586829/145380469-ec68fbf8-5f99-4875-868d-9612c4f5b6ce.png">


for example: 
C2-A3|B3=T,C1=T - returns "yes" if C2 and A3 are indepented given B3 and C1 (there is no path between C2 and A3 according to the rules), else returns "no".

### variable_elimination-
Given a query - return the propability,the numer of addition operations and the number of multiplication operations.
for example - P(D1=T|C2=v1,C3=F) A2-C1-B0-A1-B1-A3-B2-B3 -return 0.37687,83,168
the propability that D1=T given C2=V1 and C3=f is 0.37687, and it takes 83 addition operations and 168 multiplication operations.
(the variables after the parenthesis is the order of the elumination).
<br/><br/><br/><br/>
## Preper the input
the Baysian network graph shoud be in XML file like the one that given here-
for verey variable put his name and his outcomes with the title of VARIABLE:
<VARIABLE>
	<NAME>E</NAME>
	<OUTCOME>T</OUTCOME>
	<OUTCOME>F</OUTCOME>
</VARIABLE>

after doing that for all the variables, again for verey variable put his parents and his CPT with the title of DEFINITION:
<DEFINITION>
	<FOR>A</FOR>
	<GIVEN>E</GIVEN>
	<GIVEN>B</GIVEN>
	<TABLE>0.95 0.05 0.29 0.71 0.94 0.06 0.001 0.999</TABLE>
</DEFINITION>

this example is correspond to this table:

| E  | B | A | P(A\|E, B) |
| ------------- | ------------- | ------------- | ------------- |
| T  | T | T | 0.95 |
| T  | T | F | 0.05 |
| T  | F | T | 0.29 |
| T  | F | F | 0.71 |
| F  | T | T | 0.94 |
| F  | T | F | 0.06 |
| F  | F | T | 0.001 |
| F  | F | F | 0.0999 |

in the "input.txt" file - put the querys:
* for Bayes ball - before the line put the 2 variables you want to check if the are independent or not in that way: "B-E|", 
if ther is a given values in the query- in that way - B-E|J=T,A=F.
* for Variable elumination - in that way P(B=T|J=T,M=T) E-A.
the result will be in "output.txt".

there is 2 Bayesian network for the input - "alarm.xml"

<img width="379" alt="ללא שם" src="https://user-images.githubusercontent.com/74586829/144489550-98c353ed-086e-4594-a4c7-8c491ef28bf8.png">


and "big_net.xml" 


<img width="231" alt="ללא שם" src="https://user-images.githubusercontent.com/74586829/144490601-afba31f7-bb21-4846-af2c-dcf573a290ff.png">

