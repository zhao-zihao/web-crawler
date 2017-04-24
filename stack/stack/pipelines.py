# -*- coding: utf-8 -*-

# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: http://doc.scrapy.org/en/latest/topics/item-pipeline.html


import pymongo
import json

from scrapy.conf import settings
from scrapy.exceptions import DropItem
from scrapy import log

class StackPipeline(object):
    def process_item(self, item, spider):
        return item

class JsonWriterPipeline(object):

    def open_spider(self, spider):
        self.file = open('scraped-data/items.json', 'wb')
        self.file.write(bytes('[\n', 'UTF-8'))

    def close_spider(self, spider):
        self.file.write(bytes(']', 'UTF-8'))
        self.file.close()

    def process_item(self, item, spider):
        line = json.dumps(dict(item)) + "\n"
        # Python3x then string is not the same type as for Python 2.x, you must cast it to bytes (encode it).
        self.file.write(bytes(line+',', 'UTF-8'))
        return item

# class MongoDBPipeline(object):
# 
#     def __init__(self):
#         connection = pymongo.MongoClient(
#             settings['MONGODB_SERVER'],
#             settings['MONGODB_PORT']
#         )
#         db = connection[settings['MONGODB_DATABASE']]
#         self.collection = db[settings['MONGODB_COLLECTION']]
# 
#     def process_item(self, item, spider):
#         valid = True
#         for data in item:
#             if not data:
#                 valid = False
#                 raise DropItem("Missing {0}!".format(data))
#         if valid:
#             self.collection.insert(item)
#             log.msg("Question added to MongoDB database!",
#                     level=log.DEBUG, spider=spider)
#         return item

