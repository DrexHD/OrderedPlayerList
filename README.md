# Ordered Player List

## Config
```json5
{
  // Every how many ticks order changes should be checked and updated
  "updateRate": 5,
  // Display prefix metadata above the player
  "displayPrefix": true,
  // Display suffix metadata above the player
  "displaySuffix": true,
  // A list of comparisons, applied top to bottom.
  // In this example, rank weight will be checked first, then people with same rank weight will be sorted by playtime...
  "order": [
    {
      "key": "weight",
      "reversed": true,
      "mode": "integer",
      "type": "metadata"
    },
    {
      "placeholder": "player:statistic",
      "argument": "play_time",
      "reversed": true,
      "mode": "integer",
      "type": "placeholder"
    },
    {
      "placeholder": "player:pos_y",
      "reversed": false,
      "mode": "double",
      "type": "placeholder"
    },
    {
      "placeholder": "player:statistic",
      "argument": "deaths",
      "reversed": false,
      "mode": "integer",
      "type": "placeholder"
    },
    {
      "placeholder": "player:name",
      "reversed": false,
      "mode": "string",
      "ignoreCase": true,
      "type": "placeholder"
    }
  ]
}
```
## Comparisons
Comparisons allow to define how players should be ordered in the player list. They are applied top to bottom. That means
if a comparison was evaluated to be equal for two players, the next one down will be used to determine player order instead.

It may sometimes be beneficial to interpret data as an integer or a floating point number, instead of string comparisons.
Parsing modes are used to tell the mod how it should interpret a string to apply the correct comparison method. Available parsing modes are `integer`, `string`, `double`, `long` and `boolean`!

There are currently two different comparison types available:
### Metadata
Useful for player comparison based on metadata from mods like [luckperms](https://luckperms.net/wiki/Prefixes,-Suffixes-&-Meta#meta) 
```json5
{
  // Metadata key that should be checked against
  "key": "<key>",
  // Whether the comparison should be reversed
  "reversed": false,
  // Parsing mode
  "mode": "<mode>",
  "type": "metadata"
}
```

### Placeholders
Allows for player comparison using [placeholders](https://placeholders.pb4.eu/user/default-placeholders/)
```json5
{
  // Placeholder id (without %)
  "placeholder": "<placeholder>",
  // Optional argument used in placeholders
  "argument": "<argument>",
  // Whether the comparison should be reversed
  "reversed": false,
  // Parsing mode
  "mode": "<mode>",
  "type": "placeholder"
}
```


## Features
This mod sends fake vanilla teams to the players in order to manipulate the displayed player list order. If you are using teams to change player collisions, nametag / glow color or nameTagVisibility you can use [meta keys](https://luckperms.net/wiki/Meta-Commands) to replicate this behaviour.

| Meta Key       	        | Description                                                                                                  	                                      | Default 	  |
|-------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|------------|
| color          	        | This is used for glow and nametag color (use [Color Codes](https://minecraft.wiki/w/Formatting_codes#Color_codes)) 	                       | `reset` 	  |
| collision      	        | Player collisions are calculated client side, if you wish to disable player collisions, set this to `false`  	                                      | `true`  	  |
| nameTagVisible 	        | Whether or not nametags should be displayed above the player                                                 	                                      | `true`  	  |
| seeFriendlyInvisibles 	 | When enabled players render themselves transparent, when they have the invisibility status effect                                                 	 | `false`  	 |
| prefix 	                | Team prefix used for nametag rendering                                                 	                                                            | `""`  	    |
| suffix 	                | Team suffix used for nametag rendering                                                 	                                                            | `""`  	    |


## Limitations
- Some features from custom teams, that are being calculated by the client (glow color, collisions, nametag visibility) may not work properly (see [Features](#features))
- Players in spectator mode will always be displayed at the bottom of the list