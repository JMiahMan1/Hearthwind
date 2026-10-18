#Trader Llamas
execute as @e[type=#travel:villagers] at @s if entity @e[type=minecraft:trader_llama,tag=!occupied,tag=!cooldown,distance=..1,nbt={Tame:1b},predicate=travel:trader_carpeted,sort=nearest] run function travel:camel/villager_ride_trader_llama
execute as @e[type=minecraft:trader_llama,predicate=travel:trader_llama] run tag @s add occupied 
execute as @e[type=minecraft:trader_llama,predicate=travel:mount] run tag @s remove occupied
execute as @e[type=#travel:villagers] at @s if predicate travel:villager_ride_trader_llama run tag @s add trader_llama_riden

#Dismount Trader Llamas
execute as @e[type=#travel:villagers] if predicate travel:dismount run tag @s remove trader_llama_riden
execute as @p[advancements={travel:travel/trader_llama_riden=true}] at @s positioned ^ ^ ^4 if entity @e[type=#travel:villagers,tag=trader_llama_riden,distance=1..4,sort=nearest] run ride @e[type=#travel:villagers,tag=trader_llama_riden,limit=1,sort=nearest] dismount
execute as @p[advancements={travel:travel/trader_llama_riden=true}] run tag @e[type=minecraft:trader_llama,predicate=travel:mount] add cooldown
execute as @p[advancements={travel:travel/trader_llama_riden=true}] run advancement revoke @s only travel:travel/trader_llama_riden

schedule function travel:camel/villager_ride_trader_llama 10t