#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import sys
import sqlite3
import zlib
import re
import logging
from datetime import datetime
from sqlite3.dbapi2 import Connection, Cursor
from typing import *
from FileUtils import FileUtils

class RvfExtractHelper(object):
    def __init__(self) -> None:
        super().__init__()

    def main(self, argv: List[str]) -> None:
        logging.basicConfig(level=logging.INFO)
        try:
            argvBasePath: str = argv[1] if len(argv) >= 2 else ""
            basePath: str = self.readBasePath(argvBasePath)
            dbFileList: List[str] = self.buildDbFileList(basePath)
            for dbFile in dbFileList:
                rvfDir: str = self.initRvfDir(dbFile)
                dbConnection, dbCursor = self.connectDb(dbFile)
                self.queryRecords(dbCursor)
                self.saveAllRecords(rvfDir, dbCursor)
                self.closeDb(dbConnection, dbCursor)
        except Exception as e:
            logging.exception("!!!! ERROR:", e)

    def readBasePath(self, argvBasePath: str) -> str:
        """- 获取存储路径（文件或文件夹）。

        - param
            - `argvBasePath` 命令行传入的存储路径
        - return 获取到的存储路径（文件或文件夹）
        """
        return FileUtils.inputPath("Input SQLite DB file or dir: ", argvBasePath)

    def buildDbFileList(self, basePath: str) -> List[str]:
        """- 生成路径下所有 db 文件的列表。

        - param
            - `basePath` 存储路径
        - return 该路径下所有的 db 文件
        """
        dbFileList: List[str] = FileUtils.listAllPaths(basePath, type=FileUtils.FILE, \
            extList=["db"])
        logging.info("Building db file list finished.")
        return dbFileList

    def initRvfDir(self, dbFile: str) -> str:
        """- 初始化存储文件夹。

        - param
            - `dbFile` 数据库文件名
        - return 要存储到的目标文件夹
        """
        # 存储到数据库同名文件夹下，路径相同
        prefix: str = os.path.splitext(dbFile)[0]
        # 加上时间戳前缀
        suffix: str = datetime.now().strftime("%Y%m%d-%H%M%S")
        rvfDir: str = prefix + '-' + suffix
        if not os.path.isdir(rvfDir):
            os.makedirs(rvfDir)
        logging.info("rvfDir = %s" % rvfDir)
        return rvfDir

    def connectDb(self, dbFile: str) -> Tuple[Connection, Cursor]:
        """- 连接到数据库。

        - param
            - `dbFile` 数据库文件名
        - return 元组，参数为 (数据库连接, 数据库游标)
        """
        dbConnection: Connection = sqlite3.connect(dbFile)
        dbCursor: Cursor = dbConnection.cursor()
        logging.info("Database %s opened." % dbFile)
        return (dbConnection, dbCursor)

    def queryRecords(self, dbCursor: Cursor) -> Cursor:
        """- 查询数据库。

        - param
            - `dbCursor` 数据库游标
        - return 游标
        """
        sql: str = """
        select c.`fid` id, c.`内容` data, t.`标题` title
        from `资料库` c
        left join `标题` t on t.`ID` = c.`fid`
        where t.`标题` != '说明文档'
            and data is not null
        """
        dbCursor.execute(sql)
        logging.info("Records fetched %s, %s, %s." \
            % (dbCursor.arraysize, dbCursor.rowcount, dbCursor.lastrowid))
        return dbCursor

    def saveAllRecords(self, rvfDir: str, dbCursor: Cursor) -> None:
        """- 保存所有记录中的数据。

        - param
            - `rvfDir` 存储文件夹
            - `dbCursor` 数据库游标
        """
        for record in dbCursor:
            logging.info("Saving row id = %s, title = %s..." % (record[0], record[2]))
            rvfFile, rvfData = self.__extractRecord(record)
            # 替换掉不合法文件名的字符
            finalRvfFile: str = re.sub(r"[\\/:*?<>|]", "_", rvfFile).strip()
            # 替换尾部多余的 _ 和空格
            finalRvfFile = re.sub(r"[ _]+$", "", finalRvfFile)
            fullRvfFile: str = "%s/%s" % (rvfDir, finalRvfFile)
            self.__writeFile(fullRvfFile, rvfData)
        logging.info("DONE!")

    def closeDb(self, dbConnection: Connection, dbCursor: Cursor) -> None:
        """- 关闭数据库。

        - param
            - `dbConnection` 数据库连接
        """
        if dbCursor != None:
            dbCursor.close()
        if dbConnection != None:
            dbConnection.close()

    def __extractRecord(self, record: Tuple[int, bytes, str]) -> Tuple[str, bytes]:
        """- 解析记录，生成文件名和文件内容。

        - param
            - `record` 记录
        """
        id: int = record[0]
        data: bytes = record[1]
        title: str = record[2]
        rvfFile: str = "%05d-%s.rvf" % (id, title)
        rvfData: bytes = zlib.decompress(data)
        return (rvfFile, rvfData)

    def __writeFile(self, rvfFile: str, rvfData: bytes) -> None:
        """- 写入记录到文件中。

        - param
            - `rvfDir` 存储文件夹
            - `rvfFile` 文件名
            - `rvfData` 内容
        """
        with open(rvfFile, "wb") as file:
            file.write(rvfData)
            logging.info("Saved file: %s" % rvfFile)

if __name__ == "__main__":
    RvfExtractHelper().main(sys.argv)
