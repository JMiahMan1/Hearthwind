#Llamas
execute as @e[type=#travel:villagers] at @s if entity @e[type=minecraft:llama,tag=!occupied,tag=!cooldown,distance=..1,nbt={Tame:1b},predicate=travel:carpeted,sort=nearest] run function travel:camel/villager_ride_llama
execute as @e[type=minecraft:llama,predicate=travel:llama] run tag @s add occupied 
execute as @e[type=minecraft:llama,predicate=travel:mount] run tag @s remove occupied
execute as @e[type=#travel:villagers] at @s if predicate travel:villager_ride_llama run tag @s add llama_riden

#Dismount Llamas
execute as @e[type=#travel:villagers] if predicate travel:dismount run tag @s remove llama_riden
execute as @p[advancements={travel:travel/llama_riden=true}] at @s positioned ^ ^ ^4 if entity @e[type=#travel:villagers,tag=llama_riden,distance=1..4,sort=nearest] run ride @e[type=#travel:villagers,tag=llama_riden,limit=1,sort=nearest] dismount
execute as @p[advancements={travel:travel/llama_riden=true}] run tag @e[type=minecraft:llama,predicate=travel:mount] add cooldown
execute as @p[advancements={travel:travel/llama_riden=true}] run advancement revoke @s only travel:travel/llama_riden

schedule function travel:camel/villager_ride_llama 10t