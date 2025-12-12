import json
import string
import os

DATA_DIR = "src/main/resources/data/thermia"

ENTITY_DATA_MAP_DIR = "data_maps/entity_types"
ENTITY_DATA_MAP_FILE = "entity_temperatures.json"

BLOCK_DATA_MAP_DIR = "data_maps/block"
BLOCK_DATA_MAP_FILE = "block_temperature.json"

def mobTemp(baseDict: dict, resourceID: string, maxTemp: float, minTemp: float, isMob: bool, isTamed: bool) -> dict:
    
    baseDictValues = baseDict["values"]

    baseDictValues[resourceID] = {}
    baseDictValues[resourceID]["max_entity_temperature"] = maxTemp
    baseDictValues[resourceID]["min_entity_temperature"] = minTemp
    baseDictValues[resourceID]["is_mob"] = isMob
    baseDictValues[resourceID]["is_tamed"] = isTamed

    return baseDict

def blockTemp(baseDict: dict, resourceID: string, temp: float, searchCap: int, hasTFCHeat: bool) -> dict:

    baseDictValues = baseDict["values"]
    
    baseDictValues[resourceID] = {}
    baseDictValues[resourceID]["temperature"] = temp
    baseDictValues[resourceID]["search_cap"] = searchCap
    baseDictValues[resourceID]["has_tfc_heat"] = hasTFCHeat

    return baseDict

def writeJson(jsonDict: dict, dir: string, filename: string) -> None:

    if (not os.path.exists(dir)):
        os.makedirs(dir)

    f = open(dir + "/" + filename, 'w')
    f.write(json.dumps(jsonDict, indent=4))
    f.close()

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
    entityDict = mobTemp(entityDict, "tfc:horse", 40, -13, True, True)
    entityDict = mobTemp(entityDict, "minecraft:player", 40, 10, False, False)

    for v in entityDict.values():
        for k, v2 in v.items():
            print(k, v2)

    writeJson(entityDict, DATA_DIR + "/" + ENTITY_DATA_MAP_DIR, ENTITY_DATA_MAP_FILE)
    print(f"Written to {DATA_DIR}/{ENTITY_DATA_MAP_DIR}/{ENTITY_DATA_MAP_FILE}")

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
    blockDict = blockTemp(blockDict, "tfc:crucible", 0, 16, True)
    blockDict = blockTemp(blockDict, "tfc:fire_pit", 0, 16, True)
    blockDict = blockTemp(blockDict, "tfc:channel", 0, 16, True)
    blockDict = blockTemp(blockDict, "tfc:mold_table", 0, 16, True)

    for v in blockDict.values():
        for k, v2 in v.items():
            print(k, v2)

    writeJson(blockDict, DATA_DIR + "/" + BLOCK_DATA_MAP_DIR, BLOCK_DATA_MAP_FILE)
    print(f"Written TO {DATA_DIR}/{BLOCK_DATA_MAP_DIR}/{BLOCK_DATA_MAP_FILE}")

if __name__== "__main__":
    main()