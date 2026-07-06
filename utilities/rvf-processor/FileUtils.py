#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import re
from typing import *

class FileUtils(object):
    ALL: int = 0
    FILE: int = 1
    DIR: int = 2

    # @staticmethod
    # def inputFile(prompt: str="", argv: str="") -> str:
    #     return FileUtils.inputPath(prompt, argv, FileUtils.FILE)

    # @staticmethod
    # def inputDir(prompt: str="", argv: str="") -> str:
    #     return FileUtils.inputPath(prompt, argv, FileUtils.DIR)

    @staticmethod
    def inputPath(prompt: str="", argv: str="", type: int=0) -> str:
        """- 获取用户输入的路径。
        - 首先根据 type 校验 argv 的合法性，如果不合法则提示用户输入路径。

        - param
            - `prompt` 输入时的提示
            - `argv` 命令行传入的路径
            - `type` 要获取的路径类型，文件（FileUtils.FILE）或文件夹（FileUtils.DIR）
        - return 获取到的路径
        - raise `FileNotFoundError` 如果最终输入的路径不合法
        """
        path: str = ""
        if FileUtils.isPathValid(argv, type):
            path = argv
        else:
            path = input(prompt)

        finalPath: str = FileUtils.prettifyPath(path)
        if not FileUtils.isPathValid(finalPath, type):
            raise FileNotFoundError("Invalid file or dir: %s" % finalPath)
        return finalPath

    @staticmethod
    def listAllPaths(path: str, type: int=0, extList: List[str]=[]) -> List[str]:
        """- 列出路径下所有的路径，使用 type 和 ext 进行过滤。

        - param
            - `type` 路径类型
            - `ext` 文件名后缀
        - return 遍历所得的路径列表
        """
        # 去除扩展名列表中多余的“.”，并且添加一个“.”
        finalExtList: List[str] = ["." + re.sub(r"^\.+", "", ext) for ext in extList]
        pathList: List[str] = []
        if (type == None or type == FileUtils.FILE) and os.path.isfile(path):
            # 如果传入的是文件则单独处理，os.walk() 不会遍历文件
            pathList.append(path)
        else:
            for parentDir, _, fileList in os.walk(path):
                if type == None or type == FileUtils.DIR:
                    pathList.append(parentDir)
                if type == None or type == FileUtils.FILE:
                    # 将符合扩展名的添加到文件列表中
                    # 如果扩展名列表为空（即不限制扩展名）或扩展名在列表中则添加到文件列表中
                    pathList.extend(["%s/%s" % (parentDir, file) for file in fileList \
                        if len(finalExtList) == 0 or os.path.splitext(file)[1] in finalExtList])
        # “美化”一下路径
        finalPathList: List[str] = [FileUtils.prettifyPath(path) for path in pathList]
        return finalPathList

    @staticmethod
    def isPathValid(path: str, type: int=0) -> bool:
        """- 根据类型判断路径是否合法。

        - param
            - `path` 路径
            - `type` 类型，文件（FileUtils.FILE）或文件夹（FileUtils.DIR）
        - return 路径是否合法
        """
        pathValid: bool = False
        if (type == FileUtils.FILE and os.path.isfile(path)) \
            or (type == FileUtils.DIR and os.path.isdir(path)) \
            or (os.path.exists(path)):
            pathValid = True
        else:
            pathValid = False
        return pathValid

    @staticmethod
    def prettifyPath(path: str) -> str:
        """- 强迫症方法，将路径中的 \\ 全部转换成 /，去除路径结尾多余的 /。

        - param
            - `path` 路径
        - return “美化”后的路径
        """
        return re.sub(r"/+$", "", path.replace("\\", "/"))

    @staticmethod
    def isFileTypeText(file: str) -> bool:
        """- 通过文件的前 256 个字节判断是否为文本文件。
        - 如果该文件前 256 个字节全都为合法的 ASCII 码，则为文本文件。

        - param
            - `file` 文件名
        - 是否为文本文件。
        """
        if not os.path.isfile(file):
            raise FileNotFoundError("Invalid file or dir: %s" % file)

        aTextFile: bool = True
        with open(file, "rb") as f:
            headerBytes: bytes = f.read(0x100)
            for header in headerBytes:
                if header != 0x0a and header != 0x0d and (header < 0x20 or header > 0x7f):
                    aTextFile = False
        return aTextFile

if __name__ == "__main__":
    # print(FileUtils.inputPath("Path: ", ""))
    # print(FileUtils.inputPath("Path Dir: ", "C:\\", type=FileUtils.DIR))
    print(FileUtils.listAllPaths("D:\\Users\\wbh\\Desktop\\test\\", extList=[".rvf"]))
