scoreboard players add @e[type=camel,tag=cooldown] dismount_cooldown 1
scoreboard players set @e[type=camel,tag=cooldown,scores={dismount_cooldown=61}] dismount_cooldown 0
execute as @e[type=camel,tag=cooldown,scores={dismount_cooldown=60}] run tag @s remove cooldown