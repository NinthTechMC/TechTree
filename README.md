# TechTree
My guidebook-ish mod

## What does this mod do?
This mod adds customizable guide pages for tech trees in mod packs. 
It has a dependency system similar to TC's research system, 
but it simply unlock entries as the player gets items. 
It has no complicated stuff like quest, rewards, etc.

## Usage
The mod adds a single guide book item and opens the GUI when used. You can navigate
through the category and items in the GUI.

When making the modpack, you can use `/techtree reload` to reload the config without
restarting the game.

## Concepts
The entries in the tech tree is broken into categories, and each category has items.
The categories and items are identified by their paths in the config directory.
You will see this referred to as the category id and the item id.

### Item Entry
Each item entry consists of a list of items that, when obtained, completes the entry.
Each entry also has a list of dependencies - a list of other item entries that need
to be completed before this entry is completable.

The full states for each item entry are:
- Hidden:
  - This is the default state for every entry
  - The entry is hidden in the guide book
- Discovered: 
  - All its dependencies are Unlocked or above
  - Entry is displayed as a dark square, with the darkened texture and `???` name
  - Entry content is not readable
- Unlocked:
  - All its dependencies are Completable or above
  - Entry is displayed as a dark square, with normal texture and readable name
  - Entry content is not readable
- Completable:
  - All dependencies Completed
  - Entry is displayed as normal square
  - Entry content is readable
- Completed: 
  - Entry must satisfy all requirements to be Completable
  - The player has obtained an item that can complete the entry

### Category
Items are grouped into categories. Each category also has an index entry that describes
what the category is. Categories also follow a similar state system, but they are progressed
automatically with item progression:
- Hidden:
  - This is the default state for every category
  - The category is hidden in the guide book
- Discovered:
  - At least one entry in the category is Discovered or above
  - Category is displayed as a dark block, with darkened texture and `???` name
  - Category content is not accessible
- Unlocked:
  - At least one entry in the category is Unlocked
  - Category is displayed normally
  - Category content is accessible
- Progressed:
  - At least one entry is Completed
  - Category will have its total number of entries and number of completed entries displayed
- Completed:
  - All entries in the category are Completed

### Tutorial Categories
Each category can be marked as a tutorial category.
Before all tutorial categories are completed, there are several things to note:
- The guide book item will be kept in the inventory when player dies ("Beginner Protection")
- The guide book won't show the number of categories in total

## Config Format

### File Format
This mod uses a custom config text format for its entries. The format is similar to TOML or INI,
where `[]` are used to denote a section. Section orders are not significant with one exception being
`[text]`. Whitespaces are also trimmed except for the text section

### Item Format
Items in the config are specified as `modid:name:meta`. `meta` is optional, and can be:
- wildcard `*` (any meta). This is the default when meta is not specified
- A single value `42`
- An interval (`41-43`), both ends are inclusive
- A list of values or intervals (`35,37-40,42`)

When specifying an icon, the first valid value in meta is used (0 if wildcard)

### Text Format
The main text for item entries, categories and the index entry are denoted in the `[text]` section.
`[text]` must be the last section in the config file, and all content afterwards are part of the text.

The text section has automatic line break and pagination. You can also control the line breaks
in a way similar to markdown, where one newline doesn't break a paragraph, but multiple does.
Consecutive newlines are also kept as empty lines.

For pagination, you can manually insert page breaks with `---` on a line by itself.

You can also display items in the text section. The syntax is:
```
<|>modid:name:meta stackSize<|>
```
Examples:
```
<|>minecraft:stone<|>
<|>minecraft:log:1<|>
<|>minecraft:log 2<|>
<|>minecraft:log:2 2<|>
```
Multiple items can be put in the same line following the same line break practice.

NBT is not supported at the moment.

### Comments
With the exception of the text section, content after the first `#` is ignored on all lines.

## Config Structure
The config root is at `.minecraft/config/TechTree`.
The paths below are relative to this root.

### Localization
Each locale needs its own copy of the pages stored at `/<locale>` (for example `/en_US`). If a locale is not found, it defaults to `en_US`.

### Index
The index is `/<locale>/index.txt`. This is what you see if you open the index book. Its format is
```
[title]
Title
Subtitle

[text]
A long long time ago, ...
```
The `[title]` section will become the item name of the guide book. The first line is the name
and the rest are tooltips.

### Category Index
The category index entry is displayed when a category is clicked but no item entry is selected yet.
Each category index is stored at `/<locale>/<category_id>.txt`. Its format is
```
[title]
Category Title

[tutorial]

[icon]
<item>

[text]
Your text goes here
...
```
The icon item will be displayed in the category. The `[tutorial]` section
will make the category a tutorial. Leave it out if it's not a tutorial.
Completing all tutorials will show how many categories are completed vs total.

### Item Entry
Item entries are stored at `/<locale>/<category_id>/<item_id>.txt`. Its format is
```
[title]
Item Title
Item Subtitle

[icon]
<item>

[items]
<item>
<item>

[after]
<item_entry>
<item_entry>

[text]
Your text goes here
```
There are 2 extra sections compared to category:
- `[items]`: each line should be an item (with modid, name and meta range) that can complete this entry
- `[after]`: each line should be an item entry that this item depends on. The id can be `category.item`, or `.item` to use the same category as this item

## Hidden Dependencies
An item can specify `hidden` as one of the dependencies:
```
[after]
hidden
<modid>:<name>
```
Item entries with `hidden` dependencies will remain in the Hidden state until it can be in the `Completed`
state (i.e. actually obtaining the item). This can be used to lock subtrees behind certain discovery, for example
a boss drop.

## Completion-in-advance
It's possible that the player obtains an item that is meant to be obtained later in the game.
In this case, the entry won't be completed until the prereqs are satisfied. Once they are,
the entry will automatically be Completed upon being Completable.

## Error Handling
If the config data is invalid, the error will be displayed as a chat message when you log in or when you reload.

## Data Structure
Each player's progress is stored in the players data directory as `<player_name>.techtree.txt`
The stored data is a list of items the player ever picked up that is can be used to progress the
tech tree. This means:

- If the tech tree changes slightly without adding new items, the progress will still be valid
- If a new item not previously reference in the tech tree is added, it is assumed that the player
has not picked it up, since only items in the tech tree is added to the save data.

## Debug Support
There is a debug guide book item available that has several features:
- Automatically reload the config when used
- Displays all pages (including hidden), and all pages can be read.
- Displays additional debug info
- Allows completing a category or item entry in the GUI.

There are also some commands which could be handy:
- `/techtree reload` to reload config
- `/techtree hand` to print the name of the item in your hand

## Multiplayer Support
Multiplayer (i.e. dedicated server) is supported. The config needs to be both on the client 
and server side.

In the future, the config maybe downloaded directly from the server, but there's no plan to do it now.
