#!/usr/bin/env python
# -*- coding: utf-8 -*-

import sys
import os
import re
import subprocess
import logging
import img2pdf
from typing import *
from FileUtils import FileUtils

class JpgToPdfHelper(object):
    def __init__(self) -> None:
        super().__init__()

    def main(self, argv: List[str]) -> None:
        logging.basicConfig(level=logging.INFO)
        try:
            argvBasePath: str = argv[1] if len(argv) >= 2 else ""
            basePath: str = self.readBasePath(argvBasePath)
            imageFileList: List[str] = self.buildImageFileList(basePath)
            imageGroupList: List[str] = self.buildImageGroupList(imageFileList)
            imageGroupDict: Dict[str, List[str]] = self.groupImageFile(imageGroupList)
            self.convertToPdf(imageGroupDict)
            logging.info("DONE!")
        except Exception as e:
            logging.exception("!!!!ERROR", e)

    def readBasePath(self, argvBasePath: str) -> str:
        """- 获取存储路径（文件或文件夹）。

        - param
            - `argvBasePath` 命令行传入的存储路径
        - return 获取到的存储路径（文件或文件夹）
        """
        return FileUtils.inputPath("Input jpg file or dir: ", argvBasePath)

    def buildImageFileList(self, basePath: str) -> List[str]:
        """- 生成路径下所有 jpg 文件的列表。

        - param
            - `basePath` 存储路径
        - return 该路径下所有的 jpg 文件
        """
        imageFileList: List[str] = FileUtils.listAllPaths(basePath, type=FileUtils.FILE, \
            extList=["jpg"])
        logging.info("Building image file list finished.")
        return imageFileList

    def buildImageGroupList(self, imageFileList: List[str]) -> List[str]:
        """- 根据 jpg 文件的名称进行分组。
        - 每组 jpg 文件的命名格式为 group1_1.jpg group1_2.jpg，则分组为 group1，最后生成的文件为 group1.pdf。

        - param
            - `imageFileList` jpg 文件列表
        - return 生成的 jpg 文件的分组列表
        """
        imageGroupSet: Set[str] = set()
        for imageFile in imageFileList:
            imageFileWithoutExt: str = os.path.splitext(imageFile)[0]
            # 去除文件名最后的形如 _1 _2 的字符串
            imageGroup: str = re.sub(r"_\d+$", "", imageFileWithoutExt)
            imageGroupSet.add(imageGroup)
        logging.info("Building image group list finished.")
        return list(imageGroupSet)

    def groupImageFile(self, imageGroupList: List[str]) -> Dict[str, List[str]]:
        """- 根据 jpg 文件的名称进行分组。
        - 假设每组 jpg 文件的命名格式为 group1_1.jpg、group1_2.jpg，则分组为 group1，最后生成的文件为 group1.pdf。

        - param
            - `imageGroupList` jpg 文件分组
        - return 生成的 jpg 文件的分组字典
        """
        imageGroupDict: Dict[str, List[str]] = {}
        for imageGroup in imageGroupList:
            imageFileList: List[str] = []
            index: int = 1
            # 根据组名添加后缀，生成新的 jpg 文件名
            imageFile: str = "%s_%s.jpg" % (imageGroup, index)
            # 如果生成的 jpg 文件存在，则一直循环添加到 jpg 文件列表中
            while os.path.isfile(imageFile):
                imageFileList.append(imageFile)
                index += 1
                imageFile = "%s_%s.jpg" % (imageGroup, index)
            imageGroupDict[imageGroup] = imageFileList
        logging.info("Grouping images finished.")
        return imageGroupDict

    def convertToPdf(self, imageGroupDict: Dict[str, List[str]]) -> None:
        """- 根据分组转换所有的 jpg 为 pdf 文件。

        - param
            - `imageGroupDict` jpg 文件的分组字典，key 为分组名，value 为文件名列表
        """
        logging.info("Starting convertion...")
        for (imageGroup, imageFileList) in imageGroupDict.items():
            self.__convertByLibrary(imageGroup, imageFileList)

    def __convertByLibrary(self, imageGroup: str, imageFileList: List[str]) -> None:
        """- 使用库转换单组 jpg 为 pdf 文件。

        - param
            - `imageGroup` 文件分组名
            - `imageFileList` 分组下的 jpg 文件列表
        """
        pdfFile: str = "%s.pdf" % (imageGroup)
        logging.info("Converting to %s..." % (pdfFile))
        with open(pdfFile, 'wb') as f:
            f.write(img2pdf.convert(imageFileList))

    def __convertByCommand(self, imageGroup: str, imageFileList: List[str]) -> None:
        """- 使用命令行转换单组 jpg 为 pdf 文件。

        - param
            - `imageGroup` 文件分组名
            - `imageFileList` 分组下的 jpg 文件列表
        """
        # 给所有的 jpg 文件添加引号 ""
        legalFileList: List[str] = [("\"%s\"" % file) for file in imageFileList]
        # 拼接成 jpg 参数列表
        imageFileArgv: str = " ".join(legalFileList)
        # 拼接成 pdf 参数
        pdfFileArgv: str = "\"%s.pdf\"" % imageGroup
        # 生成命令表达式，形如：`img2pdf "c:/group1_1.jpg" "c:/group1_2.jpg" -o "c:/group1.pdf"`
        command: str = "img2pdf %s -o %s" % (imageFileArgv, pdfFileArgv)
        logging.info("command = %s" % command)
        exitVal: int = subprocess.call(command, shell=True)
        logging.info("Command finished with value: %s" % exitVal)

if __name__ == "__main__":
    JpgToPdfHelper().main(sys.argv)
