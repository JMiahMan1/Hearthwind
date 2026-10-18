#Camels
execute as @e[type=#travel:villagers] at @s if entity @e[type=camel,tag=!occupied,tag=!cooldown,nbt={equipment:{saddle:{id:"minecraft:saddle"}}},distance=..1.25,sort=nearest] run function travel:camel/villager_ride_camel
execute as @e[type=camel,nbt={equipment:{saddle:{id:"minecraft:saddle"}}}] if predicate travel:camel run tag @s add occupied
execute as @e[type=camel,nbt={equipment:{saddle:{id:"minecraft:saddle"}}}] if predicate travel:mount run tag @s remove occupied
execute as @e[type=#travel:villagers] at @s if predicate travel:villager_ride_camel run tag @s add camel_riden

#Dismount Camels
execute as @e[type=#travel:villagers] if predicate travel:dismount run tag @s remove camel_riden
execute as @p[advancements={travel:travel/camel_riden=true}] at @s positioned ^ ^ ^4 if entity @e[type=#travel:villagers,tag=camel_riden,distance=1..4,sort=nearest] run ride @e[type=#travel:villagers,tag=camel_riden,limit=1,sort=nearest] dismount
execute as @p[advancements={travel:travel/camel_riden=true}] run tag @e[type=minecraft:camel,predicate=travel:mount] add cooldown
execute as @p[advancements={travel:travel/camel_riden=true}] run advancement revoke @s only travel:travel/camel_riden

schedule function travel:camel/villager_ride_camel 10t