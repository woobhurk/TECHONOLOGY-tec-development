# -*- coding: utf-8 -*-

from collections import deque
from typing import *

class BreadthFirstSearch():
    """- 广度优先搜索。
    """
    def __init__(self) -> None:
        super().__init__()

    def containsKey(self, objMap: Dict[Any, List[Any]], key: Any, prediction: Any) -> bool:
        """- 广度优先搜索。

        - param
            - objMap 一个图，key 为节点，value 为节点对应的其他节点的列表。
            - key 主节点。
            - prediction 函数，用于判断结果是否符合条件。
        - return 是否找到。
        """
        found: bool = False
        # 用来记录已经搜索过的值
        searchedKeyList: List[Any] = list()
        objQueue: Deque[Any] = deque()
        # 将当前 key 对应的列表加入队列进行搜索
        objQueue += objMap[key]
        while not found and len(objQueue) > 0:
            value: Any = objQueue.popleft()
            # 如果当前值已经被搜索过了，则跳过该值，以免 map 中有循环依赖导致死循环
            if value in searchedKeyList:
                continue
            searchedKeyList.append(value)
            # 判断是否符合条件
            if prediction(value):
                # 如果符合条件，跳出
                found = True
                break
            else:
                # 如果不符合条件，将当前 key 对应的列表加入队列中
                objQueue += objMap[value] if value in objMap else []
        return found

    def containsKey2(self, objMap: Dict[Any, List[Any]], key: Any, prediction: Any) -> Tuple:
        found: bool = False
        level: int = -1
        valueList: List[Any] = objMap[key]
        for value in valueList:
            if prediction(value):
                found = True
                level = 1
                break
        if not found:
            pass

        return (found, level)

def test_containsKey():
    objMap: Dict[str, List[str]] = dict()
    objMap["main"] = ["a", "b", "c"]
    #objMap["a"] = []
    objMap["b"] = ["d", "e"]
    objMap["d"] = ["e", "f", "g"]
    objMap["f"] = ["main", "g"]
    containsKey: bool = BreadthFirstSearch().containsKey(objMap, "main", lambda key: key == "g")
    print("is in map: %s" % (containsKey))
    pass

if __name__ == "__main__":
    test_containsKey()
