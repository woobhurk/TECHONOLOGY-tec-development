# -*- coding: utf-8 -*-

import math
from typing import *

class DijkstraAlgorithm():
    """- 狄克斯特拉算法，用于搜索到目的地所占权重最小的路径。
    """
    def __init__(self) -> None:
        super().__init__()

    def search(self, objMap: Dict[Any, Dict[Any, float]], startNode: Any, endNode: Any) -> Any:
        """- 狄克斯特拉搜索。

        - param
            - objMap 要搜索的图。
            - startNode 起点。
            - endNode 终点。
        - return 最短的路径。
        """
        # 用于记录到每个节点所需的消耗
        # 初始化起点对应的节点的消耗，以及到终点的消耗（假设为无限大）
        nodeCostMap: Dict[Any, float] = objMap[startNode]
        nodeCostMap[endNode] = math.inf

        # 用于记录到达每个节点所走过的父节点
        # 初始化父节点 map 为起始节点对应的节点
        parentNodeMap: Dict[Any, Any] = {}
        for node in nodeCostMap.keys():
            parentNodeMap[node] = startNode

        # 用于记录已处理过的节点
        # 初始已处理节点为空
        ignoredNodeList: List[Any] = []

        # 获取距离起点最近的节点
        currentNode: Any = self.__findLowestCostKey(nodeCostMap, ignoredNodeList)
        # 开始遍历所有的节点
        while currentNode != None and currentNode != endNode:
            # 获取当前节点的消耗
            currentCost: float = nodeCostMap[currentNode]
            # 获取当前节点的相邻节点
            neighborNodeMap: Dict[Any, float] = objMap[currentNode]
            for neighborNode in neighborNodeMap.keys():
                # 计算相邻节点到起点的消耗
                neighborCost: float = currentCost + neighborNodeMap[neighborNode]
                # 判断相邻节点是否已经计算到起点的消耗
                if neighborNode in nodeCostMap.keys():
                    # 如果已经计算消耗，则判断相邻节点消耗是否比之前计算的消耗更小
                    if neighborCost < nodeCostMap[neighborNode]:
                        # 消耗更小，更新为更小消耗
                        nodeCostMap[neighborNode] = neighborCost
                        # 更新相邻节点的父节点信息
                        parentNodeMap[neighborNode] = currentNode
                else:
                    # 如果没有计算到起点的消耗，则计算存入
                    nodeCostMap[neighborNode] = neighborCost
                    # 存入相邻节点的父节点
                    parentNodeMap[neighborNode] = currentNode
            # 将当前节点添加到已处理节点中
            ignoredNodeList.append(currentNode)
            # 继续获取下一个最近的节点
            currentNode = self.__findLowestCostKey(nodeCostMap, ignoredNodeList)
        return (parentNodeMap, self.__findLowestCostLine(parentNodeMap, endNode))

    def __findLowestCostKey(self, nodeCostMap: Dict[Any, float], ignoredNodeList: List[Any]) -> Any:
        """- 搜索 nodeCostMap 里面权重最小的节点，并且该节点不在 ignoredNodeList 中。

        - param
            - nodeCostMap 要搜索的 map，key 为节点，value 为节点消耗。
            - ignoredNodeList 不处理的节点。
        - return 权重最小的节点。
        """
        lowestKey: Any = None
        lowestCost: Any = math.inf
        for node in nodeCostMap.keys():
            cost: float = nodeCostMap[node]
            if cost < lowestCost and node not in ignoredNodeList:
                lowestKey = node
                lowestCost = cost
        return lowestKey

    def __findLowestCostLine(self, parentNodeMap: Dict[Any, Any], endNode: Any) -> List[Any]:
        lineList: List[Any] = []
        parentNode: Any = endNode
        while parentNode in parentNodeMap:
            lineList.insert(0, parentNode)
            parentNode = parentNodeMap[parentNode]
        lineList.insert(0, parentNode)

        return lineList


def test_search():
    """
            3               3
      +------------>a---------------+
      |                             |
      |                             |
      |     2               5       v
    START---------->b-------------->d
      |             |               |
      |             |               |
      |     4       v       5       v
      +------------>c-------------->END
                    |               ^
                    |   3       2   |
                    +------>e-------+
    """
    objMap: Dict[str, Dict[str, float]] = {}
    objMap["START"] = {
        "a": 3,
        "b": 2,
        "c": 4
    }
    objMap["a"] = {
        "d": 3
    }
    objMap["b"] = {
        "c": 1,
        "d": 5
    }
    objMap["c"] = {
        "e": 3,
        "END": 5
    }
    objMap["d"] = {
        "END": 4
    }
    objMap["e"] = {
        "END": 2
    }
    parentNodeMap: Dict[str] = DijkstraAlgorithm().search(objMap, "START", "END")
    print(parentNodeMap)

if __name__ == "__main__":
    test_search()
