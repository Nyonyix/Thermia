import json
import os

DATA_DIR = "src/main/resources/data/thermia"
MINECRAFT_DATA_DIR = "src/main/resources/data/minecraft"
TAG_DIR = "tags"

ENTITY_DATA_MAP_DIR = "data_maps/entity_type"
ENTITY_DATA_MAP_FILE = "entity_temperatures.json"

BLOCK_DATA_MAP_DIR = "data_maps/block"
BLOCK_DATA_MAP_FILE = "block_temperature.json"

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

def main():

    entityDict = {"values": {}}
    entityDict = mobTemp(entityDict, "tfc:pig", 35, -10, True, True)
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

    blockDict = {"values": {}}
    blockDict = blockTemp(blockDict, "minecraft:lava", 1200, 8, False)
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

    writeJson(damage_type({}, "hyperthermia", "never", 0, effects="burning"), DATA_DIR + "/" + DAMAGE_DIR, HYPERTHERMIA_DAMAGE_FILE)
    writeJson(damage_type({}, "hypothermia", "never", 0, effects="freezing"), DATA_DIR + "/" + DAMAGE_DIR, HYPOTHERMIA_DAMAGE_FILE)

    no_knockback_tag = toTag({"values": []}, "thermia:hyperthermia")
    no_knockback_tag = toTag(no_knockback_tag, "thermia:hypothermia")
    writeJson(no_knockback_tag, MINECRAFT_DATA_DIR + "/" + TAG_DIR + "/damage_type", "no_knockback.json")

if __name__== "__main__":
    main()