#Boats & Minecarts
execute as @e[type=#travel:villagers] at @s if predicate travel:villager_ride run tag @s add riden
execute as @e[type=#travel:villagers] if predicate travel:dismount run tag @s remove riden
execute as @p[advancements={travel:travel/riden=true}] at @s positioned ^ ^ ^4 if entity @e[type=#travel:villagers,tag=riden,distance=1..4,sort=nearest] run ride @e[type=#travel:villagers,tag=riden,limit=1,sort=nearest] dismount
execute as @p[advancements={travel:travel/riden=true}] run tag @e[type=#travel:villager_rideable,predicate=travel:mount] add cooldown
execute as @p[advancements={travel:travel/riden=true}] run advancement revoke @s only travel:travel/riden