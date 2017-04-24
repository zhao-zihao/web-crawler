# -*- coding: utf-8 -*-
import scrapy
from scrapy.linkextractors import LinkExtractor
from scrapy.spiders import CrawlSpider, Rule

from stack.items import StackItem

import re
class StackCrawlerSpider(CrawlSpider):
    name = 'stack_crawler'
    allowed_domains = ['stackoverflow.com']
    start_urls = [
        'http://stackoverflow.com/questions?pagesize=50&sort=newest'
    ]

    rules = [
        Rule(LinkExtractor(allow=r'questions\?page=[0-9]&sort=newest'),
             callback='parse_item', follow=True)
    ]

    def parse_item(self, response):
        summarys = response.xpath('//div[@class="summary"]')

        for summary in summarys:
            item = StackItem()
            item['url'] = 'http://stackoverflow.com' + summary.xpath(
                'h3/a[@class="question-hyperlink"]/@href').extract()[0]
            item['title'] = summary.xpath(
                'h3/a[@class="question-hyperlink"]/text()').extract()[0]
            s = summary.xpath(
                'div[@class="excerpt"]/text()').extract()[0]
            # replace all punctuations from the plain text with space, and devide them by space
            item['excerpt'] = re.sub('[^A-Za-z0-9]+', ' ', s).strip()

            yield item

        # if "page=2" in response.url:
        #     from scrapy.shell import inspect_response
        #     inspect_response(response, self)
