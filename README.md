# ANT
Another translation plugin for Bukkit.

## Purpose
Many Minecraft communities are limited by a certain inability to allow multiple languages, primarily due to the fact that the majority of the community will not understand any foreign minorities. This is a problem, and while translation engines are not reliable, they can be used to significantly alter the circumstances, allowing more players from across the planet to play.

## REQUIREMENTS FOR RUNNING
This plugin NEEDS HitoCore. You cannot run this plugin without HitoCore present. At present, minimum version of HitoCore needed is 0.1.5. Make sure it's installed on your server before you attempt to run this plugin.

## Commands and permissions
**/ant**
Main command for the plugin.
Permission: ant.use

**/ant reload**
Permission: ant.reload
Reloads the plugin and it's configuration.

**/ant debug <number>**
Permission: ant.debug
Changes the debug status of the plugin, between levels 0, 1, or 2. Each level shows a different level of detail.

**/setlang <language>**
Permission: ant.setlang
Changes your language to what you've defined, if it's a valid language in the API class.

**/setlang <player> <language>**
Permission: ant.setlang.others
Changes another players language to that stated. Follows above criteria for language.

**/getlang <player>**
Tells you the language of the player. In 1.7 and above, the player must be online for this to work.

**/languages [pagenumber]**
Displays the available languages in a nice orderly list. Page number defaults to 1 if none entered. If a too-high number is entered, it shows the last page, and if a too-low number is entered, shows the first page.

##Dependancies for compiling
If you want to compile this locally, you need the following:

HitoCore-0.1.5.jar

I recommend using bukkit-1.9.2-R0.1.jar as your Bukkit build. If you do not use that version of Bukkit, you MUST state what you did use in your pull request.


