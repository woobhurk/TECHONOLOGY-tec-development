# -*- coding: utf-8 -*-

from typing import *

class SelectionSort():
    """- 快速排序。
    """
    def sortSelf(self, numList: List[Any]) -> List[Any]:
        """- 正序排序，将修改原列表内容。
        """
        currentIndex: int = 0
        listSize: int = len(numList)
        # 从第一个开始遍历
        while currentIndex < listSize - 1:
            # 获取最小值下标
            minIndex: int = self.__findIndexOfMinimum(numList, currentIndex)
            # 判断是否需要交换当前数和最小数
            if minIndex != currentIndex:
                # 交换当前数和最小的数
                (numList[currentIndex], numList[minIndex]) = \
                    (numList[minIndex], numList[currentIndex])
            currentIndex += 1
        return numList

    def sortNew(self, numList: List[Any]) -> List[Any]:
        """- 正序排序，生成新的排序好的列表，不影响原列表。
        """
        sortedList: List[Any] = self.sortSelf(numList[:])
        return sortedList

    def __findIndexOfMinimum(self, numList: List[Any], start: int=0) -> int:
        """- 找出列表中最小值的下标。
        """
        minIndex: int = start
        index: int = minIndex + 1
        for index in range(len(numList)):
            if numList[index] < numList[minIndex]:
                minIndex = index
        return minIndex

    def __findMinimum(self, numList: List[Any], start: int=0):
        """- 找出列表中最小值。
        """
        minIndex: int = self.__findIndexOfMinimum(numList, start)
        return numList[minIndex]

def test_sortSelf() -> None:
    from random import Random
    rand: Random = Random()
    numList: List[int] = []
    for _ in range(100):
        numList.append(rand.randint(0, 1000))
    sortedList: List[int] = SelectionSort().sortSelf(numList)
    print(sortedList)
    pass

if __name__ == "__main__":
    test_sortSelf()
