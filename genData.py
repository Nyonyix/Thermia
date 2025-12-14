import json
import os

DATA_DIR = "src/main/resources/data/thermia"
MINECRAFT_DATA_DIR = "src/main/resources/data/minecraft"
TAG_DIR = "tags"

ENTITY_DATA_MAP_DIR = "data_maps/entity_type"
ENTITY_DATA_MAP_FILE = "entity_temperatures.json"

BLOCK_DATA_MAP_DIR = "data_maps/block"
BLOCK_DATA_MAP_FILE = "block_temperature.json"

ITEM_DATA_MAP_DIR = "data_maps/item"
ITEM_INSULATION_DATA_MAP_FILE = "item_insulation.json"

DAMAGE_DIR = "damage_type"
HYPERTHERMIA_DAMAGE_FILE = "hyperthermia.json"
HYPOTHERMIA_DAMAGE_FILE = "hypothermia.json"

def toTag(baseDict: dict[str, list[str]], resourceID: str) -> dict:

    baseDict["values"].append(resourceID)

    return baseDict

def mobTemp(baseDict: dict[str, dict], resourceID: str, maxTemp: float, minTemp: float, isMob: bool, isTamed: bool) -> dict:
    
    baseDictValues = baseDict["values"]

    baseDictValues[resourceID] = {}
    baseDictValues[resourceID]["max_entity_temperature"] = maxTemp
    baseDictValues[resourceID]["min_entity_temperature"] = minTemp
    baseDictValues[resourceID]["is_mob"] = isMob
    baseDictValues[resourceID]["is_tamed"] = isTamed

    return baseDict

def blockTemp(baseDict: dict[str, dict], resourceID: str, temp: float, searchCap: int, hasTFCHeat: bool) -> dict:

    baseDictValues = baseDict["values"]
    
    baseDictValues[resourceID] = {}
    baseDictValues[resourceID]["temperature"] = temp
    baseDictValues[resourceID]["search_cap"] = searchCap
    baseDictValues[resourceID]["has_tfc_heat"] = hasTFCHeat

    return baseDict

def itemInsulation(baseDict: dict[str, dict], resourceID: str, insulationModifier: float) -> dict:

    baseDictValues = baseDict["values"]

    baseDictValues[resourceID] = {}
    baseDictValues[resourceID]["insulation_modifier"] = insulationModifier

    return baseDict

def damage_type(baseDict: dict[str, any], messageId: str, scaling: str, exhaustion: float, effects: str = "hurt", deathMessageType: str = "default") -> dict:

    baseDict["message_id"] = messageId
    baseDict["scaling"] = scaling
    baseDict["exhaustion"] = exhaustion
    baseDict["effects"] = effects
    baseDict["death_message_type"] = deathMessageType

    return baseDict

def writeJson(jsonDict: dict, dir: str, filename: str) -> None:

    if (not os.path.exists(dir)):
        os.makedirs(dir)

    f = open(dir + "/" + filename, 'w')
    f.write(json.dumps(jsonDict, indent=4))
    f.close()

    print(f"Written {dir}/{filename}")
    print()

def main():

    entityDict = mobTemp({"values": {}}, "tfc:pig", 35, -10, True, True)
    entityDict = mobTemp(entityDict, "tfc:rabbit", 40, -16, True, True)
    entityDict = mobTemp(entityDict, "tfc:cow", 35, -10, True, True)
    entityDict = mobTemp(entityDict, "tfc:goat", 25, -12, True, True)
    entityDict = mobTemp(entityDict, "tfc:yak", -11, -30, True, True)
    entityDict = mobTemp(entityDict, "tfc:alpaca", 20, -8, True, True)
    entityDict = mobTemp(entityDict, "tfc:sheep", 30, 1, True, True)
    entityDict = mobTemp(entityDict, "tfc:musk_ox", -1, -25, True, True)
    entityDict = mobTemp(entityDict, "tfc:chicken", 40, 14, True, True)
    entityDict = mobTemp(entityDict, "tfc:duck", 30, -25, True, True)
    entityDict = mobTemp(entityDict, "tfc:quail", 15, -15, True, True)
    entityDict = mobTemp(entityDict, "tfc:donkey", 40, -15, True, True)
    entityDict = mobTemp(entityDict, "tfc:mule", 40, -15, True, True)
    entityDict = mobTemp(entityDict, "tfc:horse", 40, -15, True, True)
    entityDict = mobTemp(entityDict, "minecraft:player", 40, 10, False, False)

    for v in entityDict.values():
        for k, v2 in v.items():
            print(k, v2)

    writeJson(entityDict, DATA_DIR + "/" + ENTITY_DATA_MAP_DIR, ENTITY_DATA_MAP_FILE)

    blockDict = blockTemp({"values": {}}, "minecraft:lava", 1200, 8, False)
    blockDict = blockTemp(blockDict, "tfc:rock/magma/granite", 800, 8, False)
    blockDict = blockTemp(blockDict, "tfc:rock/magma/diorite", 800, 8, False)
    blockDict = blockTemp(blockDict, "tfc:rock/magma/gabbro", 800, 8, False)
    blockDict = blockTemp(blockDict, "tfc:rock/magma/rhyolite", 800, 8, False)
    blockDict = blockTemp(blockDict, "tfc:rock/magma/basalt", 800, 8, False)
    blockDict = blockTemp(blockDict, "tfc:rock/magma/andesite", 800, 8, False)
    blockDict = blockTemp(blockDict, "tfc:rock/magma/dacite", 800, 8, False)
    blockDict = blockTemp(blockDict, "minecraft:magma_block", 800, 8, False)
    blockDict = blockTemp(blockDict, "tfc:pit_kiln", 1800, 16, False)
    blockDict = blockTemp(blockDict, "minecraft:fire", 600, 16, False)
    blockDict = blockTemp(blockDict, "tfc:spring_water", 90, 16, False)
    blockDict = blockTemp(blockDict, "tfc:bloomery", 90, 16, False)
    # blockDict = blockTemp(blockDict, "tfc:molten", 90, 16, False)
    blockDict = blockTemp(blockDict, "tfc:crucible", 0, 16, True)
    blockDict = blockTemp(blockDict, "tfc:channel", 0, 16, True)
    blockDict = blockTemp(blockDict, "tfc:mold_table", 0, 16, True)
    blockDict = blockTemp(blockDict, "tfc:firepit", 0, 16, True)
    blockDict = blockTemp(blockDict, "tfc:pot", 0, 16, True)
    blockDict = blockTemp(blockDict, "tfc:grill", 0, 16, True)
    blockDict = blockTemp(blockDict, "tfc:blast_furnace", 0, 16, True)
    blockDict = blockTemp(blockDict, "tfc:charcoal_forge", 0, 16, True)

    for v in blockDict.values():
        for k, v2 in v.items():
            print(k, v2)
    
    writeJson(blockDict, DATA_DIR + "/" + BLOCK_DATA_MAP_DIR, BLOCK_DATA_MAP_FILE)

    itemInsulationDict = itemInsulation({"values": {}}, "tfc:straw", 0.6)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:mud/entisol", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:mud/aridisol", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:mud/oxisol", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:mud/fluvisol", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:mud/andisol", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:mud/podzol", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:mud/alfisol", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:mud/mollisol", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:clay_ball", 0.5)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:kaolin_clay", 0.6)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:fire_clay", 0.6)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:plant/cherry_leaves", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:plant/green_apple_leaves", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:plant/lemon_leaves", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:plant/olive_leaves", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:plant/orange_leaves", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:plant/peach_leaves", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:plant/plum_leaves", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:plant/red_apple_leaves", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:plant/moss", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:plant/pistia", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:plant/red_algae", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:plant/reindeer_lichen", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:plant/sargassum", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:plant/winged_kelp", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:plant/leafy_kelp", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:plant/arundo", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/acacia", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/ash", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/aspen", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/birch", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/blackwood", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/chestnut", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/douglas_fir", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/hickory", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/kapok", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/mangrove", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/maple", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/oak", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/palm", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/pine", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/rosewood", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/sequoia", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/spruce", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/sycamore", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/white_cedar", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/leaves/willow", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/acacia", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/ash", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/aspen", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/birch", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/blackwood", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/chestnut", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/douglas_fir", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/hickory", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/kapok", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/mangrove", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/maple", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/oak", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/palm", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/pine", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/rosewood", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/sequoia", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/spruce", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/sycamore", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/white_cedar", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wood/fallen_leaves/willow", 0.3)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:paper", 0.5)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:unrefined_paper", 0.6)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:burlap_cloth", 0.6)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:jute_fiber", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:silk_cloth", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:string", 0.5)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wool_cloth", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wool_yarn", 0.5)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:wool", 0.9)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:leather", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:small_raw_hide", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:medium_raw_hide", 0.5)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:large_raw_hide", 0.6)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:small_prepared_hide", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:medium_prepared_hide", 0.5)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:large_prepared_hide", 0.6)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:small_scraped_hide", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:medium_scraped_hide", 0.5)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:large_scraped_hide", 0.6)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:small_soaked_hide", 0.4)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:medium_soaked_hide", 0.5)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:large_soaked_hide", 0.6)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:small_sheepskin_hide", 0.5)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:medium_sheepskin_hide", 0.6)
    itemInsulationDict = itemInsulation(itemInsulationDict, "tfc:large_sheepskin_hide", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:white_wool", 0.9)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:pink_wool", 0.9)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:red_wool", 0.9)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:blue_wool", 0.9)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:magenta_wool", 0.9)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:brown_wool", 0.9)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:orange_wool", 0.9)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:yellow_wool", 0.9)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:lime_wool", 0.9)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:green_wool", 0.9)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:black_wool", 0.9)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:light_gray_wool", 0.9)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:gray_wool", 0.9)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:cyan_wool", 0.9)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:light_blue_wool", 0.9)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:purple_carpet", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:white_carpet", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:pink_carpet", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:red_carpet", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:blue_carpet", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:magenta_carpet", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:brown_carpet", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:orange_carpet", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:yellow_carpet", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:lime_carpet", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:green_carpet", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:black_carpet", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:light_gray_carpet", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:gray_carpet", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:cyan_carpet", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:light_blue_carpet", 0.7)
    itemInsulationDict = itemInsulation(itemInsulationDict, "minecraft:purple_carpet", 0.7)
    

    for v in itemInsulationDict.values():
        for k, v2 in v.items():
            print(k, v2)
    
    writeJson(itemInsulationDict, DATA_DIR + "/" + ITEM_DATA_MAP_DIR, ITEM_INSULATION_DATA_MAP_FILE)

    writeJson(damage_type({}, "hyperthermia", "never", 0, effects="burning"), DATA_DIR + "/" + DAMAGE_DIR, HYPERTHERMIA_DAMAGE_FILE)
    writeJson(damage_type({}, "hypothermia", "never", 0, effects="freezing"), DATA_DIR + "/" + DAMAGE_DIR, HYPOTHERMIA_DAMAGE_FILE)

    no_knockback_tag = toTag({"values": []}, "thermia:hyperthermia")
    no_knockback_tag = toTag(no_knockback_tag, "thermia:hypothermia")
    writeJson(no_knockback_tag, MINECRAFT_DATA_DIR + "/" + TAG_DIR + "/damage_type", "no_knockback.json")

if __name__== "__main__":
    main()