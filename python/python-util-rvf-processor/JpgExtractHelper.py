#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import os
import logging
from typing import *
from FileUtils import FileUtils

class JpgExtractHelper(object):
    def __init__(self) -> None:
        super().__init__()

    def main(self, argv: List[str]) -> None:
        logging.basicConfig(level=logging.INFO)
        try:
            argvBasePath: str = argv[1] if len(argv) >= 2 else ""
            basePath: str = self.readBasePath(argvBasePath)
            rvfFileList: List[str] = self.buildRvfFileList(basePath)
            self.extractAll(rvfFileList)
        except Exception as e:
            logging.exception("!!!! ERROR", e)

    def readBasePath(self, argvBasePath: str) -> str:
        """- 获取存储路径（文件或文件夹）。

        - param
            - `argvBasePath` 命令行传入的存储路径
        - return 获取到的存储路径（文件或文件夹）
        """
        return FileUtils.inputPath("Input rvf file or dir: ", argvBasePath)

    def buildRvfFileList(self, basePath: str) -> List[str]:
        """- 生成路径下所有 rvf 文件的列表。

        - param
            - `basePath` 存储路径
        - return 该路径下所有的 rvf 文件
        """
        rvfFileList: List[str] = FileUtils.listAllPaths(basePath, type=FileUtils.FILE, \
            extList=["rvf"])
        logging.info("Building rvf file list finished.")
        return rvfFileList

    def extractAll(self, rvfFileList: List[str]) -> None:
        """- 提取 rvf 文件中所有的 jpg 文件。

        - param
            - `rvfFileList` rvf 文件列表
        """
        for rvfFile in rvfFileList:
            jpgDataList: List[bytes] = []
            if self.isRvfTextFile(rvfFile):
                jpgDataList = self.__readJpgTextData(rvfFile)
            else:
                jpgDataList = self.__readJpgBinaryData(rvfFile)
            self.__saveAllJpgFiles(rvfFile, jpgDataList)
        logging.info("DONE!")

    def isRvfTextFile(self, rvfFile: str) -> bool:
        """- 判断 rvf 文件是文本类型还是二进制类型。

        - param
            - `rvfFile` rvf 文件
        - return 是否是文本类型
        """
        return FileUtils.isFileTypeText(rvfFile)

    def __readJpgTextData(self, rvfFile: str) -> List[bytes]:
        """- 从文本型 rvf 文件中提取 jpg 数据。

        - param
            - `rvfFile` rvf 文件
        - return 提取到的 jpg 二进制数据列表
        """
        logging.info("Extracting jpg text from %s..." % rvfFile)
        jpgDataList: List[bytes] = []
        with open(rvfFile, "rt", encoding="utf-8") as f:
            lineList: List[str] = f.readlines()
            index: int = 1
            while index < len(lineList):
                prevLine: str = lineList[index - 1].strip()
                currentLine: str = lineList[index].strip()
                if prevLine == "TJPEGImage":
                    jpgDataList.append(bytes.fromhex(currentLine))
                index += 1
        return jpgDataList

    def __readJpgBinaryData(self, rvfFile: str) -> List[bytes]:
        """- 从二进制类型 rvf 文件中提取 jpg 数据。

        - param
            - `rvfFile` rvf 文件
        - return 提取到的 jpg 二进制数据列表
        """
        JPG_FILE_HEADER: bytes = b"\xff\xd8\xff\xe0"
        JPG_FILE_TAIL: bytes = b"\xff\xd9"
        BUFFER_SIZE: int = 1024

        logging.info("Extracting jpg binary from %s..." % rvfFile)
        jpgDataList: List[bytes] = []
        rvfFileSize: int = os.path.getsize(rvfFile)
        with open(rvfFile, "rb") as f:
            pointer: int = 0
            while pointer < rvfFileSize:
                f.seek(pointer)
                data = f.read(BUFFER_SIZE)
                headerIndex: int = data.find(JPG_FILE_HEADER)
                # 判断是否找到了 jpg 文件头
                if headerIndex >= 0:
                    # 如果找到了：
                    jpgData: bytes = bytes()
                    # 指针移动到 jpg 文件头处
                    pointer += headerIndex
                    f.seek(pointer)
                    # 向后开始读取 jpg 数据
                    data = f.read(BUFFER_SIZE)
                    tailIndex: int = data.find(JPG_FILE_TAIL)
                    # 进入循环，直到读取到 jpg 文件尾或指针到达 rvf 文件尾
                    while tailIndex < 0 and pointer < rvfFileSize:
                        # 拼接 jpg 数据
                        jpgData += data
                        # 向后读取
                        pointer += BUFFER_SIZE
                        f.seek(pointer)
                        data = f.read(BUFFER_SIZE)
                        tailIndex = data.find(JPG_FILE_TAIL)
                    # 判断是否读取到 jpg 文件尾。
                    # 如果读取到文件尾，则上面的循环会直接跳出，最后读取的那一段数据就没有被拼接进来，因此需要在这里添加
                    if tailIndex >= 0:
                        # 截取剩下的一段数据
                        lastData: bytes = data[:tailIndex]
                        # 拼接剩下的一段数据以及 jpg 文件尾到数据中
                        jpgData += lastData + JPG_FILE_TAIL
                        # 把指针向后移动
                        pointer += len(lastData)
                    jpgDataList.append(jpgData)
                else:
                    # 如果没找到：往后查找
                    pointer += BUFFER_SIZE
        return jpgDataList

    def __saveAllJpgFiles(self, rvfFile: str, jpgDataList: List[bytes]) -> None:
        """- 保存所有 jpg 文件数据。

        - param
            - `rvfFile` rvf 文件
            - `jpgDataList` jpg 二进制数据列表
        """
        index: int = 0
        while index < len(jpgDataList):
            jpgFile: str = "%s_%s.jpg" % (os.path.splitext(rvfFile)[0], index + 1)
            jpgData: bytes = jpgDataList[index]
            self.__saveJpgFile(jpgFile, jpgData)
            index += 1

    def __saveJpgFile(self, jpgFile: str, jpgData: bytes) -> None:
        """- 保存 jpg 文件。

        - param
            - `jpgFile` jpg 文件名
            - `jpgData` 文件内容
        """
        with open(jpgFile, "wb") as f:
            f.write(jpgData)

if __name__ == "__main__":
    JpgExtractHelper().main(sys.argv)
