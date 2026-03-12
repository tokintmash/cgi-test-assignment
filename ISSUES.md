# Issues

Raw findings from manual testing. Resolved issues get moved to PROBLEMS.md with a proper writeup.

## Reservation
[x] Remove the reservation popup between search and floor plan blocks. It makes the design jump. Instead, add the confirmation inside the table modal  
[x] Reset reservations should have a confirmation modal with Yes/No and put the confirmation message in there as well.
[] Currenlty, a reserved table cannot be clicked, making cancelling reservation impossible. Make it open in a modal and add functionality for cancelling a resevation.  

## Layout
[x] Make T3 bottom to line up with T2 and T4  
[x] Tooltips of uppermost tables open up and are cut by the viewBox. Make them open downward  
[x] The hight of floor plan and recommendations panes should not lessen if there are less results than vertical space

## Design
[x] Suggest a nicer color scheme for tables.  
[x] Add a favicon. Something funny related to food.  
[x] As the tables open in a modal, there is not so much use for the "selected" state as there is for the "hover". Leave the selected, but add also hover.


## Search / recommendations
[x] For 1-2 people party should not be recommended tables larger than 4 seats
[x] For 3-4 people party should not be recommended tables larger than 6 seats
[x] "No matches" should appear if there are no results at all. It should no appear if there are combinations available  
[x] Currenlty the recommandations are limited to 5. Change it so that there is no numerical limit for the recommendations (without changing the matching logic). For this, add scroll to the right pane, but only show in the floor plan those that are visible in the right pane.  
[x] Remove the combinations-subtitle
