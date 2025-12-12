import json
import string
import os

DATA_DIR = "src/main/resources/data/thermia"

ENTITY_DATA_MAP_DIR = "data_maps/entity_types"
ENTITY_DATA_MAP_FILE = "Entity_Temperatures.json"

BLOCK_DATA_MAP_DIR = "data_maps/block"
BLOCK_DATA_MAP_FILE = "Block_Temperature.json"

def mobTemp(baseDict: dict, resourceID: string, maxTemp: float, minTemp: float, isMob: bool, isTamed: bool) -> dict:
    
    baseDict[resourceID] = {}
    baseDict[resourceID]["max_entity_temperature"] = maxTemp
    baseDict[resourceID]["min_entity_temperature"] = minTemp
    baseDict[resourceID]["is_mob"] = isMob
    baseDict[resourceID]["is_tamed"] = isTamed

    return baseDict

def writeJson(jsonDict: dict, dir: string, filename: string) -> None:

    if (not os.path.exists(dir)):
        os.makedirs(dir)

    f = open(dir + "/" + filename, 'w')
    f.write(json.dumps(jsonDict, indent=4))
    f.close()

def main():

    entityDict = {}
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

    for k,v in entityDict.items():
        print(k, v)

    writeJson(entityDict, DATA_DIR + "/" + ENTITY_DATA_MAP_DIR, ENTITY_DATA_MAP_FILE)

if __name__== "__main__":
    main()