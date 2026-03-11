# Issues

Raw findings from manual testing. Resolved issues get moved to PROBLEMS.md with a proper writeup.

## Reservation
[] Remove the reservation popup between search and floor plan blocks. It makes the design jump. Instead, add the confirmation inside the table modal  
[] Reset reservations should have a confirmation modal with Yes/No and put the confirmation message in there as well.

## Layout
[] Make T3 bottom to line up with T2 and T4
[] The hight of floor plan and recommendations panes should not lessen if there are less results than vertical space

## Design
[] Suggest a nicer color scheme for tables.
[] Add a favicon. Something funny related to food.
[] As the tables open in a modal, there is not so much use for the "selected" state as there is for the "hover". Leave the selected, but add also hover.

## Search / recommendations
[] "No matches" should appear if there are no results at all. It should no appear if there are combinations available
[] Currenlty the recommandations are limited to 5. Change it so that there is no numerical limit for the recommendations (without changing the matching logic). For this, add scroll to the right pane, but only show in the floor plan those that are visible in the right pane.
[] Remove the combinations-subtitle