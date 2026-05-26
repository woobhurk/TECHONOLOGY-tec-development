# -*- coding: utf-8 -*-

from typing import *

class QuickSort():
    """- 快速排序。
    """
    def __init__(self) -> None:
        super().__init__()

    def sort(self, numList: List[Any]) -> List[Any]:
        """- 排序。
        """
        sortedList: List[Any] = []
        # 判断列表大小是否大于一个
        if len(numList) > 1:
            # 如果大于一个元素：
            # 选择第一个数作为基准数
            numIndex: int = len(numList) // 2
            num: Any = numList[numIndex]
            numList.remove(num)
            # 找出更小的数
            smallerNumList: List[Any] = self.__filterNumLt(numList[1:], num)
            # 找出更大的数
            biggerNumList: List[Any] = self.__filterNumGeq(numList[1:], num)
            # 递归，对子列表排序，并放到基准数前面
            sortedList += self.sort(smallerNumList)
            # 基准数
            sortedList += [num]
            # 把大于基准数的所有数放到基准数后面
            sortedList += self.sort(biggerNumList)
        else:
            # 如果只有一个或没有元素：直接返回
            sortedList = numList

        return sortedList

    def __filterNumGeq(self, numList: List[Any], num: Any) -> List[Any]:
        """- 找出列表中大于等于基准数的所有数。
        """
        biggerNumList: List[Any] = [x for x in numList if x >= num]
        return biggerNumList

    def __filterNumLt(self, numList: List[Any], num: Any) -> List[Any]:
        """- 找出列表中小于基准数的所有数。
        """
        smallerNumList: List[Any] = [x for x in numList if x < num]
        return smallerNumList

def test_sort() -> None:
    from random import Random
    random: Random = Random()
    numList: List[int] = []
    for _ in range(100):
        numList.append(random.randint(0, 1000))
    print("Before sort: " + str(numList))
    sortedList: List[int] = QuickSort().sort(numList)
    print("\nAfter sort: " + str(sortedList))

if __name__ == "__main__":
    test_sort()
